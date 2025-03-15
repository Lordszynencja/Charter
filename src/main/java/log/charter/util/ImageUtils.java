package log.charter.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import log.charter.io.Logger;

public class ImageUtils {
	public static BufferedImage loadSafe(final String path) {
		return loadSafe(path, new File(RW.getJarDirectory(), path));
	}

	public static BufferedImage loadSafe(final String name, final File... files) {
		for (final File f : files) {
			try {
				return ImageIO.read(f);
			} catch (final Exception e) {
				Logger.error("Couldn't load image '" + name + "' from file " + f.getName());
			}
		}

		Logger.error("Couldn't load image " + name);
		return null;
	}

	public static BufferedImage loadSafeFromDir(final File dir, final String fileName) {
		return loadSafe(fileName, new File(dir, fileName));
	}
}
