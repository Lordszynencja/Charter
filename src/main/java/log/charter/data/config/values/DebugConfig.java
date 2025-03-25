package log.charter.data.config.values;

import static log.charter.data.config.values.accessors.BooleanValueAccessor.forBoolean;

import java.util.Map;

import log.charter.data.config.values.accessors.ValueAccessor;

public class DebugConfig {
	public static boolean logging = false;
	public static boolean frameTimes = false;
	public static boolean handleASIOInput = false;
	public static boolean showFTGraph = false;
	public static boolean showInputGraph = false;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".logging", forBoolean(v -> logging = v, () -> logging, logging));
		valueAccessors.put(name + ".frameTimes", forBoolean(v -> frameTimes = v, () -> frameTimes, frameTimes));
		valueAccessors.put(name + ".handleASIOInput",
				forBoolean(v -> handleASIOInput = v, () -> handleASIOInput, handleASIOInput));
		valueAccessors.put(name + ".showFTGraph", forBoolean(v -> showFTGraph = v, () -> showFTGraph, showFTGraph));
		valueAccessors.put(name + ".showInputGraph",
				forBoolean(v -> showInputGraph = v, () -> showInputGraph, showInputGraph));
	}
}
