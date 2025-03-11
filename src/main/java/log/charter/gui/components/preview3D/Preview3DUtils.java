package log.charter.gui.components.preview3D;

import static log.charter.data.config.GraphicalConfig.previewWindowScrollSpeed;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.values.InstrumentConfig;

public class Preview3DUtils {
	public static final double topStringPosition = 0;
	public static final double fretThickness = 0.025;
	public static final double firstFretDistance = 1.2;
	public static final double noteHalfWidth = firstFretDistance / 3;
	public static final double tailHalfWidth = noteHalfWidth * 0.33;
	public static final double stringDistance = 0.35;
	public static final double bendHalfstepDistance = stringDistance * 0.8;
	public static final int closeDistance = 250;
	public static final float closeDistanceZ = (float) getTimePosition(closeDistance);
	public static final int fadedDistance = 50;
	public static final float fadedDistanceZ = (float) getTimePosition(fadedDistance);

	public static double fretLengthMultiplier = 1;

	private static double[] fretPositions = new double[InstrumentConfig.frets + 1];
	static {
		double fretLength = firstFretDistance;
		fretPositions[0] = 0;
		for (int fret = 1; fret <= InstrumentConfig.frets; fret++) {
			fretPositions[fret] = fretPositions[fret - 1] + fretLength;
			fretLength *= fretLengthMultiplier;
		}
	}

	public static double getFretPosition(final int fret) {
		final double position = fret < 0 ? fretPositions[0]//
				: fret > InstrumentConfig.frets ? fretPositions[InstrumentConfig.frets]//
						: fretPositions[fret];

		return InstrumentConfig.leftHanded ? fretPositions[InstrumentConfig.frets] / 2 - position : position;
	}

	public static int getVisibility() {
		return (int) (1_600 * previewWindowScrollSpeed);
	}

	public static double getVisibilityZ() {
		return getTimePosition(getVisibility());
	}

	public static double getFretMiddlePosition(final int fret) {
		return (getFretPosition(fret - 1) + getFretPosition(fret)) / 2;
	}

	public static double getChartboardYPosition(final int strings) {
		return -stringDistance * strings;
	}

	private static int invertStrings(final int string, final int strings) {
		return GraphicalConfig.invertStrings3D ? strings - string - 1 : string;
	}

	public static double getStringPosition(final int string, final int strings) {
		return topStringPosition - invertStrings(string, strings) * stringDistance;
	}

	public static double getStringPositionWithBend(final int string, final int strings, final double bendValue) {
		return getStringPosition(string, strings) + bendValue * bendHalfstepDistance;
	}

	public static double getTimePosition(final double time) {
		// 0.02 is subjective and might need adjustments if camera perspective is edited
		return time * 0.02 / previewWindowScrollSpeed;
	}
}
