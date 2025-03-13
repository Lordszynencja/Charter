package log.charter.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.SystemType;

public class Utils {
	public static final String defaultConfigDir = getDefaultConfigDir();

	private static String getDefaultConfigDir() {
		final String programFolderName = new File("pom.xml").exists() ? "Charter_dev" : "Charter";

		return switch (SystemType.systemType) {
			case WINDOWS -> System.getenv("APPDATA") + "/" + programFolderName + "/";
			case MAC -> "~/Library/Preferences/" + programFolderName + "/";
			default -> "~/.local/" + programFolderName + "/";
		};
	}

	public enum TimeUnit {
		NANOSECONDS(1_000_000_000, "%dns", "%d", ".%09d"), //
		MICROSECONDS(1_000_000, "%dus", "%d", ".%06d"), //
		MILISECONDS(1_000, "%dms", "%d", ".%03d"), //
		SECONDS(60, "%ds", "%d", ":%02d"), //
		MINUTES(60, "%dmin", "%d", ":%02d"), //
		HOURS(24, "%dh", "%d", " %02d"), //
		DAYS(365, "%dd", "%dd", " %3dd"), //
		YEARS(365, "%dy", "%dy", " %dy");

		private static Map<TimeUnit, TimeUnit> nextUnits = new HashMap<>();
		static {
			nextUnits.put(NANOSECONDS, SECONDS);
			nextUnits.put(MICROSECONDS, SECONDS);
			nextUnits.put(MILISECONDS, SECONDS);
			nextUnits.put(SECONDS, MINUTES);
			nextUnits.put(MINUTES, HOURS);
			nextUnits.put(HOURS, DAYS);
			nextUnits.put(DAYS, YEARS);
			nextUnits.put(YEARS, YEARS);
		}

		public final long max;
		public final String fullFormat;
		public final String partialFrontFormat;
		public final String partialFormat;

		private TimeUnit(final long max, final String fullFormat, final String partialFrontFormat,
				final String partialFormat) {
			this.max = max;
			this.fullFormat = fullFormat;
			this.partialFrontFormat = partialFrontFormat;
			this.partialFormat = partialFormat;
		}

		public TimeUnit next() {
			return nextUnits.get(this);
		}
	}

	public static class TimeFormatter {
		private TimeUnit unit = TimeUnit.SECONDS;
		private TimeUnit minUnitShown = TimeUnit.SECONDS;
		private TimeUnit maxUnitShown = TimeUnit.HOURS;

		public TimeFormatter(final TimeUnit unit) {
			this.unit = unit;
		}

		public TimeFormatter unit(final TimeUnit unit) {
			this.unit = unit;
			return this;
		}

		public TimeFormatter minUnitShown(final TimeUnit minUnitShown) {
			this.minUnitShown = minUnitShown;
			return this;
		}

		public TimeFormatter maxUnitShown(final TimeUnit maxUnitShown) {
			this.maxUnitShown = maxUnitShown;
			return this;
		}

		public String format(final int time) {
			return formatTime(time, unit, minUnitShown, maxUnitShown);
		}
	}

	public static <T> T nvl(final T value, final T defaultValue) {
		return value == null ? defaultValue : value;
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
		return GraphicalConfig.invertStrings ? stringId : strings - stringId - 1;
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

	public static String formatTime(final long time, final TimeUnit unit, final TimeUnit minUnitShown,
			final TimeUnit maxUnitShown, final boolean partial) {
		if (minUnitShown.compareTo(unit) > 0 || (maxUnitShown.compareTo(unit) > 0 && time >= unit.max)) {
			final String prefix = formatTime(time / unit.max, unit.next(), minUnitShown, maxUnitShown, true);
			final String part = unit.partialFormat.formatted(time % unit.max);

			return prefix + part;
		}

		return (partial ? unit.partialFrontFormat : unit.fullFormat).formatted(time);
	}

	public static String formatTime(final long time, final TimeUnit unit, final TimeUnit minUnitShown,
			final TimeUnit maxUnitShown) {
		return formatTime(time, unit, minUnitShown, maxUnitShown, false);
	}

	public static String formatTime(final long time) {
		return formatTime(time, TimeUnit.SECONDS, TimeUnit.SECONDS, TimeUnit.HOURS);
	}

	public static double mix(final double start, final double end, final double position, final double startValue,
			final double endValue) {
		if (end == start) {
			return startValue;
		}

		return (endValue * (position - start) + startValue * (end - position)) / (end - start);
	}
}
