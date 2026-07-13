package mg.yoan.file.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageBlackAndWhiteConverterTest {

  private final ImageBlackAndWhiteConverter subject = new ImageBlackAndWhiteConverter();

  @Test
  void toBlackAndWhite_converts_colored_image_to_grayscale() throws IOException {
    File colored = File.createTempFile("colored-", ".png");
    try {
      BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
      image.setRGB(0, 0, Color.RED.getRGB());
      image.setRGB(1, 0, Color.GREEN.getRGB());
      image.setRGB(0, 1, Color.BLUE.getRGB());
      image.setRGB(1, 1, Color.YELLOW.getRGB());
      ImageIO.write(image, "png", colored);

      File blackAndWhite = subject.toBlackAndWhite(colored, "bw-test-");
      try {
        BufferedImage result = ImageIO.read(blackAndWhite);
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, result.getType());
        assertEquals(2, result.getWidth());
        assertEquals(2, result.getHeight());
        assertTrue(isGrayscale(result.getRGB(0, 0)));
        assertTrue(isGrayscale(result.getRGB(1, 1)));
      } finally {
        blackAndWhite.delete();
      }
    } finally {
      colored.delete();
    }
  }

  @Test
  void toBlackAndWhite_rejects_non_image_file() throws IOException {
    File textFile = File.createTempFile("not-image-", ".txt");
    try (FileWriter writer = new FileWriter(textFile)) {
      writer.write("not an image");
    }
    try {
      assertThrows(
          IllegalArgumentException.class, () -> subject.toBlackAndWhite(textFile, "bw-bad-"));
    } finally {
      textFile.delete();
    }
  }

  private static boolean isGrayscale(int rgb) {
    int r = (rgb >> 16) & 0xff;
    int g = (rgb >> 8) & 0xff;
    int b = rgb & 0xff;
    return r == g && g == b;
  }
}
