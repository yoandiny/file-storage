package mg.yoan.file.endpoint.rest.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.file.repository.model.StoredFile;
import mg.yoan.file.service.FileUploadService;
import mg.yoan.file.service.StoredFileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class FileController {

  private final FileUploadService fileUploadService;
  private final StoredFileService storedFileService;

  @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FileResponse upload(
      @RequestPart("file") MultipartFile file, @RequestPart("email") String email) {
    try {
      return toResponse(fileUploadService.upload(file, email));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
    }
  }

  @GetMapping("/files")
  public List<FileResponse> getAll() {
    return storedFileService.findAll().stream().map(this::toResponse).toList();
  }

  private FileResponse toResponse(StoredFile storedFile) {
    return new FileResponse(
        storedFile.getId(),
        storedFile.getName(),
        storedFile.getUserEmail(),
        storedFile.getCreationDatetime());
  }

  public record FileResponse(UUID id, String name, String userEmail, Instant creationDatetime) {}
}
