package log.charter.data.config.values;

import java.util.Map;

public interface ConfigValue {
	public void init(Map<String, ValueAccessor> valueAccessors, String name);
}
