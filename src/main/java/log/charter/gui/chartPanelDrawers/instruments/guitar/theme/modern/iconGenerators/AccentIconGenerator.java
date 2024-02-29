package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.iconGenerators.IconGeneratorUtils.calculateSize;
import static log.charter.util.ColorUtils.setAlpha;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.function.BiFunction;

public class AccentIconGenerator {
	private static BufferedImage generateFadingColorIcon(final Color color,
			final BiFunction<Double, Double, Double> distanceFunction) {
		final int size = calculateSize(1.4);

		final BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
		final double middle = (size - 1) / 2.0;

		for (int x = 0; x < size; x++) {
			final double dx = (x - middle) / middle;
			for (int y = 0; y < size; y++) {
				final double dy = (y - middle) / middle;
				final double d = distanceFunction.apply(dx, dy);
				final double alpha = max(0, min(1, 1 - (d - 0.8) / 0.15));
				icon.setRGB(x, y, setAlpha(color, (int) (alpha * 255)).getRGB());
			}
		}

		return icon;
	}

	public static BufferedImage generateAccentIcon(final Color color) {
		return generateFadingColorIcon(color, (x, y) -> sqrt(x * x + y * y));
	}

	public static BufferedImage generateHarmonicAccentIcon(final Color color) {
		return generateFadingColorIcon(color, (x, y) -> abs(x * 0.9) + abs(y * 0.9));
	}
}
