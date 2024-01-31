package log.charter.util;

import java.awt.Color;

public class ColorUtils {

	private static int clamp(final int colorValue) {
		return Math.max(0, Math.min(255, colorValue));
	}

	private static int clamp(final double colorValue) {
		return clamp((int) colorValue);
	}

	public static Color multiplyColor(final Color c, final double multiplier) {
		return new Color(clamp(c.getRed() * multiplier), //
				clamp(c.getGreen() * multiplier), //
				clamp(c.getBlue() * multiplier), //
				c.getAlpha());
	}

	public static Color mix(final Color c0, final Color c1, final float mix) {
		final int red = (int) (c0.getRed() * (1 - mix) + c1.getRed() * mix);
		final int green = (int) (c0.getGreen() * (1 - mix) + c1.getGreen() * mix);
		final int blue = (int) (c0.getBlue() * (1 - mix) + c1.getBlue() * mix);
		return new Color(red, blue, green);
	}
}
