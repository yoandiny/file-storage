package mg.yoan.file.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import mg.yoan.file.repository.model.StoredFile;
import mg.yoan.file.service.FileUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FileController.class)
class FileControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private FileUploadService fileUploadService;

  @Test
  void post_files_returns_stored_file_metadata() throws Exception {
    UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
    Instant createdAt = Instant.parse("2026-07-13T05:00:00Z");
    StoredFile storedFile = new StoredFile();
    storedFile.setId(id);
    storedFile.setName("photo.png");
    storedFile.setUserEmail("user@example.com");
    storedFile.setCreationDatetime(createdAt);

    when(fileUploadService.upload(any(), eq("user@example.com"))).thenReturn(storedFile);

    MockMultipartFile file =
        new MockMultipartFile("file", "photo.png", "image/png", new byte[] {1, 2, 3});

    mockMvc
        .perform(multipart("/files").file(file).param("email", "user@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.name").value("photo.png"))
        .andExpect(jsonPath("$.userEmail").value("user@example.com"))
        .andExpect(jsonPath("$.creationDatetime").value("2026-07-13T05:00:00Z"));
  }

  @Test
  void post_files_returns_bad_request_when_file_type_rejected() throws Exception {
    when(fileUploadService.upload(any(), eq("user@example.com")))
        .thenThrow(new IllegalArgumentException("Only PNG and JPG files are accepted"));

    MockMultipartFile file =
        new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[] {1, 2, 3});

    mockMvc
        .perform(multipart("/files").file(file).param("email", "user@example.com"))
        .andExpect(status().isBadRequest());

    verify(fileUploadService).upload(any(), eq("user@example.com"));
  }
}
