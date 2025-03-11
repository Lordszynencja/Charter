package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forBoolean;
import static log.charter.data.config.values.ValueAccessor.forInteger;

import java.util.Map;

public class InstrumentConfig {
	public static boolean leftHanded = false;
	public static int maxStrings = 9;
	public static int frets = 28;
	/**
	 * in half steps
	 */
	public static int maxBendValue = 6;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".leftHanded", forBoolean(v -> leftHanded = v, () -> leftHanded));
		valueAccessors.put(name + ".maxStrings", forInteger(v -> maxStrings = v, () -> maxStrings));
		valueAccessors.put(name + ".frets", forInteger(v -> frets = v, () -> frets));
		valueAccessors.put(name + ".maxBendValue", forInteger(v -> maxBendValue = v, () -> maxBendValue));
	}
}
