import java.awt.image.BufferedImage;
import java.io.File;

import boofcv.gui.ListDisplayPanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;

public class TestImage {

  static boolean debug = true;
  static boolean original = true;

  static int BLUR_CONSTANT = 1;

  static String directory = "imgs/2018PowerUp/";

  static ListDisplayPanel panel = new ListDisplayPanel();

  public static void main(String[] args) {
    // int numFiles = 1;
    // File[] files = getFiles(directory);
    // for (File f : files) {
    // String name = f.getName();
    // name = directory + name;
    //
    // System.out.println(name);
    // long x = System.currentTimeMillis();
    // ImageProcessing(name);
    // System.out.println("Runtime of File #" + numFiles + ": "
    // + (System.currentTimeMillis() - x) + "\n");
    // numFiles++;
    // }

    ImageProcessing("imgs/2018PowerUp/IMG_0264.JPG");
    ShowImages.showWindow(panel, "Box finder", true);

  }

  public static File[] getFiles(String directory) {
    return new File(directory).listFiles();

  }

  public static void ImageProcessing(String FileName) {

    BufferedImage origImage = UtilImageIO
        .loadImage(FileName);

    if (debug) {
      panel.addImage(origImage, "original");

    }
    // panel.addImage(image, "asdf2");

    BufferedImage hsvImage = FilterUtility.resizeImage(origImage, 150, 120);

    if (debug || original) {
      panel.addImage(hsvImage, "resize");
    }

    hsvImage = FilterUtility.filterSelectedHSVColor(hsvImage, 50f,
        100f, .25f, 1f, 120f, 255f);
    if (debug) {

      panel.addImage(hsvImage, "filter");
    }

    hsvImage = FilterUtility.convertToBinaryImage(hsvImage, 0);
    if (debug) {

      panel.addImage(hsvImage, "b/w");
    }

    hsvImage = FilterUtility.gaussianBlur(hsvImage, BLUR_CONSTANT);

    if (debug) {

      panel.addImage(hsvImage, "blur");
    }

    hsvImage = FilterUtility.convertToBinaryImage(hsvImage, 254);
    if (debug) {

      panel.addImage(hsvImage, "b/w");
    }

    hsvImage = FilterUtility.plotCenterPoint(hsvImage);
    panel.addImage(hsvImage, "Find Center Point of " + FileName);

  }
}
