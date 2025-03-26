package log.charter.data.config.values.accessors;

import java.util.Map;

public interface ValueAccessor {
	public void set(final String value);

	public void saveTo(final Map<String, String> config, final String name);
}