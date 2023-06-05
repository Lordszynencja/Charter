package log.charter.gui.components.preview3D;

public class Preview3DUtils {
	public static final int visibility = 6_000;
	private static final double timeZMultiplier = 0.01;
	public static final double visibilityZ = getTimePosition(visibility);

	public static double getFretPosition(final double fret) {
		return 3 * (1 - Math.pow(0.5, fret / 12.0));
	}

	public static double getFretMiddlePosition(final double fret) {
		return (getFretPosition(fret - 1) + getFretPosition(fret)) / 2;
	}

	public static double getStringPosition(final int string) {
		return -string * 0.35;
	}

	public static double getStringPositionWithBend(final int string, final double bendValue) {
		return getStringPosition(string) + bendValue * 0.3;
	}

	public static double getTimePosition(final int time) {
		return time * timeZMultiplier;
	}
}
