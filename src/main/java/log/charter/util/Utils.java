package log.charter.util;

import java.util.HashMap;
import java.util.Map;

import log.charter.data.config.Config;

public class Utils {
	public enum TimeUnit {
		MILISECONDS(1000, "%d", ".%03d"), //
		SECONDS(60, "%d", ":%02d"), //
		MINUTES(60, "%d", ":%02d"), //
		HOURS(24, "%d", " %02d"), //
		DAYS(365, "%dd", " %3dd"), //
		YEARS(365, "%dy", " %dy");

		private static Map<TimeUnit, TimeUnit> nextUnits = new HashMap<>();
		static {
			nextUnits.put(MILISECONDS, SECONDS);
			nextUnits.put(SECONDS, MINUTES);
			nextUnits.put(MINUTES, HOURS);
			nextUnits.put(HOURS, DAYS);
			nextUnits.put(DAYS, YEARS);
			nextUnits.put(YEARS, YEARS);
		}

		public final int max;
		public final String fullFormat;
		public final String partialFormat;

		private TimeUnit(final int max, final String fullFormat, final String partialFormat) {
			this.max = max;
			this.fullFormat = fullFormat;
			this.partialFormat = partialFormat;
		}

		public TimeUnit next() {
			return nextUnits.get(this);
		}
	}

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

	public static String formatTime(final int time, final TimeUnit unit, final TimeUnit minUnitShown,
			final TimeUnit maxUnitShown) {
		if (minUnitShown.compareTo(unit) > 0 || (maxUnitShown.compareTo(unit) > 0 && time >= unit.max)) {
			return formatTime(time / unit.max, unit.next(), minUnitShown, maxUnitShown)
					+ unit.partialFormat.formatted(time % unit.max);
		}

		return unit.fullFormat.formatted(time);
	}
}
