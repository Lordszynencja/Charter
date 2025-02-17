package log.charter.data.config.values;

import static log.charter.data.config.values.ValueAccessor.forBoolean;

import java.util.Map;

public class DebugConfig implements ConfigValue {
	public boolean logging = false;
	public boolean frameTimes = false;
	public boolean handleASIOInput = false;
	public boolean showFTGraph = false;
	public boolean showInputGraph = false;

	@Override
	public void init(final Map<String, ValueAccessor> valueAccessors, final String name) {
		valueAccessors.put(name + ".logging", forBoolean(v -> logging = v, () -> logging));
		valueAccessors.put(name + ".frameTimes", forBoolean(v -> frameTimes = v, () -> frameTimes));
		valueAccessors.put(name + ".handleASIOInput", forBoolean(v -> handleASIOInput = v, () -> handleASIOInput));
		valueAccessors.put(name + ".showFTGraph", forBoolean(v -> showFTGraph = v, () -> showFTGraph));
		valueAccessors.put(name + ".showInputGraph", forBoolean(v -> showInputGraph = v, () -> showInputGraph));
	}
}
