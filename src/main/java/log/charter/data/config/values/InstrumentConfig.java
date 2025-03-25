package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;

import java.util.Map;

import log.charter.data.config.values.accessors.ValueAccessor;

public class InstrumentConfig {
	public static final int maxPossibleStrings = 9;

	public static boolean leftHanded = false;
	public static int maxStrings = 9;
	public static int frets = 28;
	/**
	 * in half steps
	 */
	public static int maxBendValue = 6;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".leftHanded", forBoolean(v -> leftHanded = v, () -> leftHanded, leftHanded));
		valueAccessors.put(name + ".maxStrings", forInteger(v -> maxStrings = v, () -> maxStrings, maxStrings));
		valueAccessors.put(name + ".frets", forInteger(v -> frets = v, () -> frets, frets));
		valueAccessors.put(name + ".maxBendValue", forInteger(v -> maxBendValue = v, () -> maxBendValue, maxBendValue));
	}
}
