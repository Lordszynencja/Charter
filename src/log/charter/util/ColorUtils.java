package log.charter.util;

import java.awt.Color;

public class ColorUtils {
	public static Color mix(final Color c0, final Color c1, final float mix) {
		final int red = (int) (c0.getRed() * (1 - mix) + c1.getRed() * mix);
		final int green = (int) (c0.getGreen() * (1 - mix) + c1.getGreen() * mix);
		final int blue = (int) (c0.getBlue() * (1 - mix) + c1.getBlue() * mix);
		return new Color(red, blue, green);
	}
}
