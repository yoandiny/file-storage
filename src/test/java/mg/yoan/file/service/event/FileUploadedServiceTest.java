package mg.yoan.file.service.event;

import static java.time.Duration.ofHours;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import javax.imageio.ImageIO;
import mg.yoan.file.endpoint.event.model.FileUploaded;
import mg.yoan.file.file.ImageBlackAndWhiteConverter;
import mg.yoan.file.file.bucket.BucketComponent;
import mg.yoan.file.file.hash.FileHash;
import mg.yoan.file.file.hash.FileHashAlgorithm;
import mg.yoan.file.mail.Email;
import mg.yoan.file.mail.Mailer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileUploadedServiceTest {

  @Mock private BucketComponent bucketComponent;
  @Spy private ImageBlackAndWhiteConverter imageBlackAndWhiteConverter;
  @Mock private Mailer mailer;

  @InjectMocks private FileUploadedService subject;

  @Test
  void accept_uploads_bw_image_and_emails_presigned_link() throws Exception {
    File original = File.createTempFile("original-", ".png");
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    image.setRGB(0, 0, Color.RED.getRGB());
    ImageIO.write(image, "png", original);

    when(bucketComponent.download("uploads/file-1/photo.png")).thenReturn(original);
    when(bucketComponent.upload(any(File.class), eq("bw/file-1.png")))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "hash"));
    when(bucketComponent.presign("bw/file-1.png", ofHours(24)))
        .thenReturn(URI.create("https://example.com/bw/file-1.png").toURL());

    subject.accept(
        FileUploaded.builder()
            .fileId("file-1")
            .bucketKey("uploads/file-1/photo.png")
            .userEmail("user@example.com")
            .fileName("photo.png")
            .build());

    verify(bucketComponent).upload(any(File.class), eq("bw/file-1.png"));
    verify(bucketComponent).presign("bw/file-1.png", ofHours(24));

    ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    Email email = emailCaptor.getValue();
    assertEquals("user@example.com", email.to().getAddress());
    assertTrue(email.htmlBody().contains("https://example.com/bw/file-1.png"));
    assertTrue(email.htmlBody().contains("photo.png"));
    assertTrue(!original.exists());
  }
}
