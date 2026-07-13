package mg.yoan.file.file;

import static java.io.File.createTempFile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Component;

@Component
public class ImageBlackAndWhiteConverter {

  public File toBlackAndWhite(File source, String outputPrefix) throws IOException {
    BufferedImage original = ImageIO.read(source);
    if (original == null) {
      throw new IllegalArgumentException("Unsupported or unreadable image: " + source.getName());
    }

    BufferedImage grayscale =
        new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D graphics = grayscale.createGraphics();
    graphics.drawImage(original, 0, 0, null);
    graphics.dispose();

    File output = createTempFile(outputPrefix, ".png");
    ImageIO.write(grayscale, "png", output);
    return output;
  }
}
