package mg.yoan.file.endpoint.rest.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import mg.yoan.file.repository.model.StoredFile;
import mg.yoan.file.service.FileUploadService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
public class FileController {

  private final FileUploadService fileUploadService;

  @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FileUploadResponse upload(
      @RequestPart("file") MultipartFile file, @RequestParam("email") String email) {
    try {
      StoredFile storedFile = fileUploadService.upload(file, email);
      return new FileUploadResponse(
          storedFile.getId(),
          storedFile.getName(),
          storedFile.getUserEmail(),
          storedFile.getCreationDatetime());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(BAD_REQUEST, e.getMessage());
    }
  }

  public record FileUploadResponse(
      UUID id, String name, String userEmail, Instant creationDatetime) {}
}
