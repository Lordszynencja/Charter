package log.charter.util;

import log.charter.data.config.Config;

public class Utils {
	public static boolean mapInteger(final Integer value) {
		return value != null && value != 0;
	}

	public static int getStringPosition(final int stringId, final int strings) {
		return Config.invertStrings ? stringId : strings - stringId - 1;
	}
}
