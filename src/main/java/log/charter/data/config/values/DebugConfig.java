package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forBoolean;

import java.util.Map;

public class DebugConfig {
	public static boolean logging = false;
	public static boolean frameTimes = false;
	public static boolean handleASIOInput = false;
	public static boolean showFTGraph = false;
	public static boolean showInputGraph = false;

	public static void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".logging", forBoolean(v -> logging = v, () -> logging));
		valueAccessors.put(name + ".frameTimes", forBoolean(v -> frameTimes = v, () -> frameTimes));
		valueAccessors.put(name + ".handleASIOInput", forBoolean(v -> handleASIOInput = v, () -> handleASIOInput));
		valueAccessors.put(name + ".showFTGraph", forBoolean(v -> showFTGraph = v, () -> showFTGraph));
		valueAccessors.put(name + ".showInputGraph", forBoolean(v -> showInputGraph = v, () -> showInputGraph));
	}
}
