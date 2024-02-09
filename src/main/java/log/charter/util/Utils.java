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
}
