package mg.yoan.file.service;

import static java.util.UUID.randomUUID;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import mg.yoan.file.endpoint.event.EventProducer;
import mg.yoan.file.endpoint.event.model.FileUploaded;
import mg.yoan.file.file.bucket.BucketComponent;
import mg.yoan.file.repository.StoredFileRepository;
import mg.yoan.file.repository.model.StoredFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class FileUploadService {
  public static final String UPLOADS_PREFIX = "uploads/";
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg");
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("image/png", "image/jpg", "image/jpeg");

  private final BucketComponent bucketComponent;
  private final StoredFileRepository storedFileRepository;
  private final EventProducer eventProducer;

  @SneakyThrows
  public StoredFile upload(MultipartFile multipartFile, String email) {
    var fileName = multipartFile.getOriginalFilename();
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name is required");
    }
    validateImage(fileName, multipartFile.getContentType());

    var id = randomUUID();
    var bucketKey = UPLOADS_PREFIX + id + "/" + fileName;

    File temp = File.createTempFile("upload-", "-" + fileName);
    try {
      multipartFile.transferTo(temp);
      bucketComponent.upload(temp, bucketKey);
    } finally {
      temp.delete();
    }

    var storedFile = new StoredFile();
    storedFile.setId(id);
    storedFile.setName(fileName);
    storedFile.setUserEmail(email);
    storedFile.setCreationDatetime(Instant.now());
    storedFileRepository.save(storedFile);

    eventProducer.accept(
        List.of(
            FileUploaded.builder()
                .fileId(id.toString())
                .bucketKey(bucketKey)
                .userEmail(email)
                .fileName(fileName)
                .build()));

    return storedFile;
  }

  private void validateImage(String fileName, String contentType) {
    var extension = extensionOf(fileName);
    var normalizedContentType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    if (!ALLOWED_EXTENSIONS.contains(extension)
        || (!normalizedContentType.isBlank()
            && !ALLOWED_CONTENT_TYPES.contains(normalizedContentType))) {
      throw new IllegalArgumentException("Only PNG and JPG files are accepted");
    }
  }

  private static String extensionOf(String fileName) {
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot < 0 || lastDot == fileName.length() - 1) {
      return "";
    }
    return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
  }
}
