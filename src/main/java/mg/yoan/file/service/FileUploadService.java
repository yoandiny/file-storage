package mg.yoan.file.service;

import static java.util.UUID.randomUUID;

import java.io.File;
import java.time.Instant;
import java.util.List;
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

  private final BucketComponent bucketComponent;
  private final StoredFileRepository storedFileRepository;
  private final EventProducer eventProducer;

  @SneakyThrows
  public StoredFile upload(MultipartFile multipartFile, String email) {
    var id = randomUUID();
    var fileName = multipartFile.getOriginalFilename();
    if (fileName == null || fileName.isBlank()) {
      fileName = "unnamed";
    }
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
}
