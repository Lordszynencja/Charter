package log.charter.data.config.values;

import java.util.Map;

public interface ConfigValue {
	public void installValueAccessors(Map<String, ValueAccessor> valueAccessors, String name);
}
