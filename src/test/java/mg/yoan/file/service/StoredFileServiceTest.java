package mg.yoan.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import mg.yoan.file.repository.StoredFileRepository;
import mg.yoan.file.repository.model.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoredFileServiceTest {

  @Mock private StoredFileRepository storedFileRepository;

  @InjectMocks private StoredFileService subject;

  @Test
  void findAll_returns_files_from_repository() {
    StoredFile file = storedFile();
    when(storedFileRepository.findAll()).thenReturn(List.of(file));

    List<StoredFile> result = subject.findAll();

    assertEquals(1, result.size());
    assertEquals(file.getId(), result.get(0).getId());
    verify(storedFileRepository).findAll();
  }

  private static StoredFile storedFile() {
    StoredFile file = new StoredFile();
    file.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    file.setName("photo.png");
    file.setUserEmail("user@example.com");
    file.setCreationDatetime(Instant.parse("2026-07-13T05:00:00Z"));
    return file;
  }
}
