package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;
import static log.charter.data.config.values.accessors.IntValueAccessor.forInteger;

import java.util.Map;

import log.charter.data.config.values.accessors.ValueAccessor;

public class GPConfig {
	public static boolean generateFHP = false;
	public static int slideInSize = 2;
	public static int slideOutSize = 5;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".generateFHP", forBoolean(v -> generateFHP = v, () -> generateFHP, generateFHP));
		valueAccessors.put(name + ".slideInSize", forInteger(v -> slideInSize = v, () -> slideInSize, slideInSize));
		valueAccessors.put(name + ".slideOutSize", forInteger(v -> slideOutSize = v, () -> slideOutSize, slideOutSize));
	}
}
