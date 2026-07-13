package mg.yoan.file.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import mg.yoan.file.repository.model.StoredFile;
import mg.yoan.file.service.FileUploadService;
import mg.yoan.file.service.StoredFileService;
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
  @MockBean private StoredFileService storedFileService;

  @Test
  void post_files_returns_stored_file_metadata() throws Exception {
    StoredFile storedFile = storedFile();
    when(fileUploadService.upload(any(), eq("user@example.com"))).thenReturn(storedFile);

    MockMultipartFile file =
        new MockMultipartFile("file", "photo.png", "image/png", new byte[] {1, 2, 3});
    MockMultipartFile email =
        new MockMultipartFile("email", "", "text/plain", "user@example.com".getBytes());

    mockMvc
        .perform(multipart("/files").file(file).file(email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(storedFile.getId().toString()))
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
    MockMultipartFile email =
        new MockMultipartFile("email", "", "text/plain", "user@example.com".getBytes());

    mockMvc.perform(multipart("/files").file(file).file(email)).andExpect(status().isBadRequest());

    verify(fileUploadService).upload(any(), eq("user@example.com"));
  }

  @Test
  void get_files_returns_all_stored_files() throws Exception {
    when(storedFileService.findAll()).thenReturn(List.of(storedFile()));

    mockMvc
        .perform(get("/files"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("11111111-1111-1111-1111-111111111111"))
        .andExpect(jsonPath("$[0].name").value("photo.png"))
        .andExpect(jsonPath("$[0].userEmail").value("user@example.com"));
  }

  private static StoredFile storedFile() {
    StoredFile storedFile = new StoredFile();
    storedFile.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    storedFile.setName("photo.png");
    storedFile.setUserEmail("user@example.com");
    storedFile.setCreationDatetime(Instant.parse("2026-07-13T05:00:00Z"));
    return storedFile;
  }
}
