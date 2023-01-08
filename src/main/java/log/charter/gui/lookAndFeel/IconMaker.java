package log.charter.gui.lookAndFeel;

import java.awt.Color;
import java.awt.image.BufferedImage;

class IconMaker {
	public static BufferedImage createIcon(final int width, final int height, final int[][] colorMap,
			final Color[] colors) {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < colorMap.length; y++) {
			final int[] row = colorMap[y];
			for (int x = 0; x < row.length; x++) {
				final int color = row[x];
				if (color > 0) {
					image.setRGB(x, y, colors[color].getRGB());
				}
			}
		}

		return image;
	}
}
