/**
 *
 */
package de.terrestris.momo.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * An utility class for image/graphics processing.
 *
 * @author Nils Bühner
 *
 */
public class GraphicsUtil {

	/**
	 * Converts a byte Array to an {@link BufferedImage}
	 *
	 * @param imgBytes
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage byteArrayToImage(byte[] imgBytes) throws IOException {
		BufferedImage bufferedImage = null;
		InputStream inputStream = new ByteArrayInputStream(imgBytes);
		bufferedImage = ImageIO.read(inputStream);
		return bufferedImage;
	}

	/**
	 *
	 * Credits go to http://stackoverflow.com/a/665483 and
	 * http://www.logikdev.com/2011/10/05/make-image-backgrounds-transparent-with-tolerance/
	 *
	 * @param im
	 * @param colorToMakeTransparent
	 * @param tolerance
	 *
	 * @return
	 */
	public static Image makeColorTransparent(final BufferedImage im, final Color colorToMakeTransparent, final int tolerance) {

		final ImageFilter transparencyfilter = new RGBImageFilter() {

			@Override
			public int filterRGB(int x, int y, int rgb) {
				final Color filterColor = new Color(rgb);

				if(colorsAreSimilar(filterColor, colorToMakeTransparent, tolerance)) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// Nothing to do
					return rgb;
				}

			}

		};

		final ImageProducer ip = new FilteredImageSource(im.getSource(), transparencyfilter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	/**
	 *
	 * @param image
	 * @return
	 */
	public static BufferedImage imageToBufferedImage(Image image) {

		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();

		return bufferedImage;
	}

	/**
	 * Credits: http://www.java-gaming.org/index.php?topic=32741.0
	 *
	 * Checks whether the two colors are similar for a given tolerance (in the
	 * sense of a distance between the RGB values).
	 *
	 * @param c1
	 * @param c2
	 * @param tolerance
	 * @return Whether or not the two colors are similar for the given tolerance.
	 */
	public static boolean colorsAreSimilar(final Color c1, final Color c2, final int tolerance) {
		int r1 = c1.getRed();
		int g1 = c1.getGreen();
		int b1 = c1.getBlue();
		int r2 = c2.getRed();
		int g2 = c2.getGreen();
		int b2 = c2.getBlue();

		return ((r2 - tolerance <= r1) && (r1 <= r2 + tolerance) &&
				(g2 - tolerance <= g1) && (g1 <= g2 + tolerance) &&
				(b2 - tolerance <= b1) && (b1 <= b2 + tolerance));
	}

}
