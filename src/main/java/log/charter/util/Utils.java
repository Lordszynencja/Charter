package log.charter.util;

import log.charter.data.config.Config;

public class Utils {
	public static boolean mapInteger(final Integer value) {
		return value != null && value != 0;
	}

	public static int stringId(final int string, final int strings) {
		if (strings <= 6) {
			return string;
		}

		final int offset = strings - 6;
		if (string >= offset) {
			return string - offset;
		}

		return strings - string - 1;
	}

	public static int getStringPosition(final int stringId, final int strings) {
		return Config.invertStrings ? stringId : strings - stringId - 1;
	}

	public static boolean isDottedFret(final int fret) {
		return fret % 12 == 0 || fret % 12 == 3 || fret % 12 == 5 || fret % 12 == 7 || fret % 12 == 9;
	}

	public static String formatBendValue(final int quarterStepsCount) {
		final int fullSteps = quarterStepsCount / 4;
		final int quarters = quarterStepsCount % 4;

		if (fullSteps == 0) {
			if (quarters == 0) {
				return "0";
			}
			if (quarters == 1) {
				return "¼";
			}
			if (quarters == 2) {
				return "½";
			}
			if (quarters == 3) {
				return "¾";
			}
		}

		String text = fullSteps + "";
		if (quarters == 1) {
			text += " ¼";
		}
		if (quarters == 2) {
			text += " ½";
		}
		if (quarters == 3) {
			text += " ¾";
		}

		return text;
	}
}
