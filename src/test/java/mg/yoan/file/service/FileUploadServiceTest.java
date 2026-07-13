package mg.yoan.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import mg.yoan.file.endpoint.event.EventProducer;
import mg.yoan.file.endpoint.event.model.FileUploaded;
import mg.yoan.file.file.bucket.BucketComponent;
import mg.yoan.file.file.hash.FileHash;
import mg.yoan.file.file.hash.FileHashAlgorithm;
import mg.yoan.file.repository.StoredFileRepository;
import mg.yoan.file.repository.model.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

  @Mock private BucketComponent bucketComponent;
  @Mock private StoredFileRepository storedFileRepository;
  @Mock private EventProducer eventProducer;

  @InjectMocks private FileUploadService subject;

  @ParameterizedTest
  @CsvSource({"photo.png,image/png", "photo.jpg,image/jpeg", "photo.jpeg,image/jpeg"})
  void upload_stores_in_s3_persists_metadata_and_publishes_event(
      String fileName, String contentType) {
    var multipart = new MockMultipartFile("file", fileName, contentType, new byte[] {1, 2, 3, 4});
    when(bucketComponent.upload(any(), anyString()))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "hash"));
    when(storedFileRepository.save(any(StoredFile.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    StoredFile saved = subject.upload(multipart, "user@example.com");

    assertNotNull(saved.getId());
    assertEquals(fileName, saved.getName());
    assertEquals("user@example.com", saved.getUserEmail());
    assertNotNull(saved.getCreationDatetime());

    ArgumentCaptor<String> bucketKeyCaptor = ArgumentCaptor.forClass(String.class);
    verify(bucketComponent).upload(any(), bucketKeyCaptor.capture());
    assertEquals(
        FileUploadService.UPLOADS_PREFIX + saved.getId() + "/" + fileName,
        bucketKeyCaptor.getValue());

    verify(storedFileRepository).save(saved);

    ArgumentCaptor<Collection> eventsCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(eventProducer).accept(eventsCaptor.capture());
    List<?> events = List.copyOf(eventsCaptor.getValue());
    assertEquals(1, events.size());
    FileUploaded event = (FileUploaded) events.get(0);
    assertEquals(saved.getId().toString(), event.getFileId());
    assertEquals(bucketKeyCaptor.getValue(), event.getBucketKey());
    assertEquals("user@example.com", event.getUserEmail());
    assertEquals(fileName, event.getFileName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"doc.pdf", "notes.txt", "archive.zip", "image.gif", "no-extension"})
  void upload_rejects_non_png_jpg_files(String fileName) {
    var multipart =
        new MockMultipartFile("file", fileName, "application/octet-stream", new byte[] {1});

    var error =
        assertThrows(IllegalArgumentException.class, () -> subject.upload(multipart, "a@b.com"));

    assertEquals("Only PNG and JPG files are accepted", error.getMessage());
    verify(bucketComponent, never()).upload(any(), anyString());
    verify(storedFileRepository, never()).save(any());
    verify(eventProducer, never()).accept(any());
  }

  @Test
  void upload_rejects_missing_file_name() {
    var multipart = new MockMultipartFile("file", null, "image/png", new byte[] {9});

    var error =
        assertThrows(IllegalArgumentException.class, () -> subject.upload(multipart, "a@b.com"));

    assertEquals("File name is required", error.getMessage());
    verify(bucketComponent, never()).upload(any(), anyString());
  }
}
