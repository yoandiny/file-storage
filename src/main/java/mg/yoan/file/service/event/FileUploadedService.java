package mg.yoan.file.service.event;

import static java.time.Duration.ofHours;

import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mg.yoan.file.endpoint.event.model.FileUploaded;
import mg.yoan.file.file.ImageBlackAndWhiteConverter;
import mg.yoan.file.file.bucket.BucketComponent;
import mg.yoan.file.mail.Email;
import mg.yoan.file.mail.Mailer;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class FileUploadedService implements Consumer<FileUploaded> {
  public static final String BW_PREFIX = "bw/";

  private final BucketComponent bucketComponent;
  private final ImageBlackAndWhiteConverter imageBlackAndWhiteConverter;
  private final Mailer mailer;

  @Override
  @SneakyThrows
  public void accept(FileUploaded event) {
    File original = bucketComponent.download(event.getBucketKey());
    File blackAndWhite = null;
    try {
      blackAndWhite =
          imageBlackAndWhiteConverter.toBlackAndWhite(original, "bw-" + event.getFileId());
      var bwBucketKey = BW_PREFIX + event.getFileId() + ".png";
      bucketComponent.upload(blackAndWhite, bwBucketKey);

      var downloadUrl = bucketComponent.presign(bwBucketKey, ofHours(24));
      mailer.accept(
          new Email(
              new InternetAddress(event.getUserEmail()),
              List.of(),
              List.of(),
              "Your black and white file is ready",
              """
              <p>Hello,</p>
              <p>Your file <b>%s</b> has been converted to black and white.</p>
              <p><a href="%s">Download the black and white version</a></p>
              <p>This link expires in 24 hours.</p>
              """
                  .formatted(event.getFileName(), downloadUrl),
              List.of()));
      log.info("Black and white file emailed for fileId={}", event.getFileId());
    } finally {
      original.delete();
      if (blackAndWhite != null) {
        blackAndWhite.delete();
      }
    }
  }
}
