import java.awt.image.BufferedImage;

import com.github.sarxos.webcam.Webcam;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.webcamcapture.UtilWebcamCapture;

/**
 * Processes a video feed and tracks points using KLT
 *
 * @author Peter Abeles
 */
public class VideoFeed {
  static int BLUR_CONSTANT = 1;

  public static void main(String[] args) {

    // Open a webcam at a resolution close to 640x480
    Webcam webcam = UtilWebcamCapture.openDefault(150, 120);
    // Webcam webcam = UtilWebcamCapture.openDevice("Microsoft® LifeCam
    // HD-3000",
    // 150, 120);

    // Create the panel used to display the image and feature tracks
    ImagePanel gui = new ImagePanel();
    gui.setPreferredSize(webcam.getViewSize());

    ShowImages.showWindow(gui, "KLT Tracker", true);

    while (true) {
      BufferedImage image = webcam.getImage();

      // image = IsolateTape.resizeImage(image, 150, 120);
      image = FilterUtility.filterSelectedHSVColor(image, 50f,
          100f, .25f, 1f, 120f, 255f);

      image = FilterUtility.convertToBinaryImage(image, 0);

      image = FilterUtility.gaussianBlur(image, BLUR_CONSTANT);

      image = FilterUtility.plotCenterPoint(image);

      gui.setBufferedImageSafe(image);
    }
  }
}
