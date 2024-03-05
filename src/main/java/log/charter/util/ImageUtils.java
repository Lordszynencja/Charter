package log.charter.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import log.charter.io.Logger;

public class ImageUtils {
	public static BufferedImage loadSafe(final String path) {
		try {
			return ImageIO.read(new File(path));
		} catch (final Exception e) {
			Logger.error("Couldn't load image for path " + path, e);
			return null;
		}
	}
}
