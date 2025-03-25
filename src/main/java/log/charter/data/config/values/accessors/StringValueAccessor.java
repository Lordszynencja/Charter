package log.charter.data.config.values.accessors;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringValueAccessor implements ValueAccessor {
	public static StringValueAccessor forString(final Consumer<String> setter, final Supplier<String> getter,
			final String defaultValue) {
		return new StringValueAccessor(setter, getter, defaultValue);
	}

	private final Consumer<String> setter;
	private final Supplier<String> getter;
	private final String defaultValue;

	private StringValueAccessor(final Consumer<String> setter, final Supplier<String> getter,
			final String defaultValue) {
		this.setter = setter;
		this.getter = getter;
		this.defaultValue = defaultValue;
	}

	@Override
	public void set(final String value) {
		setter.accept(value == null ? defaultValue : value);
	}

	@Override
	public void saveTo(final Map<String, String> config, final String name) {
		if (getter == null) {
			return;
		}

		final String value = getter.get();
		if (Objects.equals(defaultValue, value)) {
			return;
		}

		config.put(name, value);
	}

}
