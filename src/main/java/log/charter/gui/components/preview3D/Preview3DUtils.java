package log.charter.gui.components.preview3D;

import log.charter.data.config.Config;

public class Preview3DUtils {
	public static final int visibility = 6_000;
	private static final double timeZMultiplier = 0.01;
	public static final double visibilityZ = getTimePosition(visibility);
	public static double topStringPosition = 0;
	public static double stringDistance = 0.35;

	public static double getFretPosition(final double fret) {
		final double disposition = 3 * (1 - Math.pow(0.5, fret / 12.0));
		return Config.leftHanded ? 1.5 - disposition : disposition;
	}

	public static double getFretMiddlePosition(final double fret) {
		return (getFretPosition(fret - 1) + getFretPosition(fret)) / 2;
	}

	public static double getChartboardYPosition(final int strings) {
		return -stringDistance * strings;
	}

	private static int invertStrings(final int string, final int strings) {
		return Config.invertStrings ? strings - string - 1 : string;
	}

	public static double getStringPosition(final int string, final int strings) {
		return topStringPosition - invertStrings(string, strings) * stringDistance;
	}

	public static double getStringPositionWithBend(final int string, final int strings, final double bendValue) {
		return getStringPosition(string, strings) + bendValue * 0.3;
	}

	public static double getTimePosition(final int time) {
		return time * timeZMultiplier;
	}
}
