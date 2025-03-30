package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

import log.charter.data.config.values.accessors.ValueAccessor;

public class SecretsConfig {
	public static boolean optionsUnlocked = false;

	public static boolean aprilFoolsEnabled = true;
	public static boolean explosions = true;
	public static boolean explosionsShakyCam = false;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".aprilFoolsEnabled",
				forBoolean(v -> aprilFoolsEnabled = v, () -> aprilFoolsEnabled, aprilFoolsEnabled));
		valueAccessors.put(name + ".explosions", forBoolean(v -> explosions = v, () -> explosions, explosions));
		valueAccessors.put(name + ".explosionsShakyCam",
				forBoolean(v -> explosionsShakyCam = v, () -> explosionsShakyCam, explosionsShakyCam));
	}

	public static boolean isDate(final Month month, final int day) {
		final LocalDate date = LocalDate.now();
		return date.getMonth() == month && date.getDayOfMonth() == day;
	}

	public static boolean isAprilFools() {
		return isDate(Month.APRIL, 1);
	}

	public static boolean explosionsEnabled() {
		if (isAprilFools() && aprilFoolsEnabled) {
			return true;
		}

		return explosions;
	}

	public static boolean explosionsShakyCamEnabled() {
		if (isAprilFools() && aprilFoolsEnabled) {
			return true;
		}

		return explosionsShakyCam;
	}
}
