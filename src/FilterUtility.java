import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public class FilterUtility {

  /***
   * Filters out any values out side of the specified hues and lightnesses.
   *
   * @param image
   *          an input of BufferedImage to filter the colors out from
   * @param minHueDegree
   *          minimum hue (in degrees from 0-360) allowed
   * @param maxHueDegree
   *          maximum hue (in degrees from 0-360) allowed
   * @param minSat
   *          minimum saturation (0-255) allowed
   * @param maxSat
   *          maximum saturation (0-255) allowed
   * @param minValue
   *          minimum value, or lightness (0-255) allowed
   * @param maxValue
   *          maximum value, or lightness (0-255) allowed
   * @return returns a BufferedImage with all non-selected areas painted black
   */
  public static BufferedImage filterSelectedHSVColor(
      BufferedImage image, float minHueDegree,
      float maxHueDegree, float minSat, float maxSat, float minValue,
      float maxValue) {

    // Make a planar image of the given image. Does not have any values; it's
    // just for getting size
    Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(image, null,
        true, GrayF32.class);

    // Create a new image with the same image size
    Planar<GrayF32> hsv = input.createSameShape();

    // Convert into HSV
    ColorHsv.rgbToHsv_F32(input, hsv);

    // Extract hue and value bands which are independent of saturation
    GrayF32 H = hsv.getBand(0);
    GrayF32 S = hsv.getBand(1);
    GrayF32 V = hsv.getBand(2);

    // Makes a new BufferedImage that is completely black
    BufferedImage output = new BufferedImage(input.width, input.height,
        BufferedImage.TYPE_INT_RGB);

    // step through each pixel and find its hue and values
    for (int y = 0; y < hsv.height; y++) {
      for (int x = 0; x < hsv.width; x++) {
        // Get values for hue and value
        float dh = H.unsafe_get(x, y);
        float ds = S.unsafe_get(x, y);
        float dv = V.unsafe_get(x, y);

        // Hue is an angle in radians, so simple subtraction doesn't
        // work
        float hueDegree = (float) ((dh * 180) / Math.PI);
        float saturationValue = ds;
        float brightnessValue = dv;

        // Test if each pixel is within the range of brightness, saturation, and
        // hue
        if (minValue <= brightnessValue && maxValue >= brightnessValue) {
          if (minSat <= saturationValue && maxSat >= saturationValue) {
            if (minHueDegree <= hueDegree && maxHueDegree >= hueDegree) {

              // If pixel is within the range, then add color back
              output.setRGB(x, y, image.getRGB(x, y));
            }
          }
        }
      }
    }

    return output;
  }

  /***
   * Finds and draws the center of the white pixels of a black an white binary
   * image
   *
   * @param binaryImage
   *          An input of a bufferedimage that is black/white to find the center
   *          of. Black is
   * @return a new bufferedimage with a pinpointed center
   */
  public static BufferedImage plotCenterPoint(BufferedImage binaryImage) {

    // Make a planar image the same size as the given image. This gives us the
    // dimensions of the image, but does NOT have any color values
    Planar<GrayF32> input = ConvertBufferedImage.convertFromMulti(binaryImage,
        null, true, GrayF32.class);

    // The new bufferedImage output
    BufferedImage output = new BufferedImage(input.width, input.height,
        BufferedImage.TYPE_INT_RGB);

    // Total count variable
    long numWhitePixels = 0;
    long sumX = 0;
    long sumY = 0;

    // white
    int white = ((255) << 16) | ((255) << 8) | (255);

    // step through each pixel and find its value. Add it to the total counts
    for (int x = 0; x < input.width; x++) {
      for (int y = 0; y < input.height; y++) {
        int dv = (int) input.getBand(0).get(x, y);

        if (dv == 255) {
          numWhitePixels++;
          sumX += x;
          sumY += y;
          output.setRGB(x, y, white);
        }
      }
    }

    // prevent divide by 0 error
    if (numWhitePixels == 0) {
      return output;
    }

    // calculate average, which is the center point
    int avgX = (int) (sumX / numWhitePixels);
    int avgY = (int) (sumY / numWhitePixels);

    // size of displayed center point
    int size = 2;

    // the point's color is: red 255, green 0, blue 0 (pure red)
    int color = ((255) << 16) | ((0) << 8) | (0);

    // display center point
    for (int i = -size; i < size; i++) {
      for (int j = -size; j < size; j++) {
        int x = avgX + i;
        int y = avgY + j;

        // prevent index out of bounds if the center is near the edge
        if (x >= 0 && x < input.width) {
          if (y >= 0 && y < input.height) {
            output.setRGB(x, y, color);
          }
        }
      }
    }
    return output;
  }

  /***
   * A binary image is an image with two values - black or white
   *
   * @param image
   *          a BufferedImage to turn black or white
   * @param threshold
   *          anything with brightness value over the threshold will be set to
   *          black; others will become white.
   * @return a new BufferedImage that is completely black or white
   */
  public static BufferedImage convertToBinaryImage(BufferedImage image,
      float threshold) {
    GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null,
        GrayF32.class);
    GrayU8 binary = new GrayU8(input.width, input.height);
    ThresholdImageOps.threshold(input, binary, threshold, false);

    BufferedImage visualBinary = VisualizeBinaryData.renderBinary(binary, false,
        null);
    return visualBinary;
  }

  /***
   * Takes an image and scales it down to the set width and height
   *
   * @param img
   *          the bufferedimage to scale down
   * @param newW
   *          the width to change to
   * @param newH
   *          the height to change to
   * @return a new bufferedimage that is resized
   */
  public static BufferedImage resizeImage(BufferedImage img, int newW,
      int newH) {
    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    BufferedImage dimg = new BufferedImage(newW, newH,
        BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2d = dimg.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();

    return dimg;
  }

  /***
   * Blurs the image with a Gaussian function
   *
   * @param image
   *          the image to be blurred
   * @param radius
   *          the size "intensity" of the blur
   * @return
   *         the new blurred image
   */
  public static BufferedImage gaussianBlur(BufferedImage image, int radius) {
    GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null,
        GrayF32.class);
    GrayF32 blurred = GBlurImageOps.gaussian(input, null, 0.0, radius,
        null);
    return ConvertBufferedImage.convertTo(blurred, null);
  }

}
