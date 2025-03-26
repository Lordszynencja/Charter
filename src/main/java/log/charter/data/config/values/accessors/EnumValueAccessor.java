package log.charter.data.config.values.accessors;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumValueAccessor<T extends Enum<T>> implements ValueAccessor {
	public static <T extends Enum<T>> EnumValueAccessor<T> forEnum(final Class<T> type, final Consumer<T> setter,
			final Supplier<T> getter, final T defaultValue) {
		return new EnumValueAccessor<>(type, setter, getter, defaultValue);
	}

	private final Class<T> type;
	private final Consumer<T> setter;
	private final Supplier<T> getter;
	private final T defaultValue;

	private EnumValueAccessor(final Class<T> type, final Consumer<T> setter, final Supplier<T> getter,
			final T defaultValue) {
		this.type = type;
		this.setter = setter;
		this.getter = getter;
		this.defaultValue = defaultValue;
	}

	@Override
	public void set(final String value) {
		T v;
		try {
			v = Enum.valueOf(type, value);
		} catch (final Exception e) {
			v = defaultValue;
		}

		setter.accept(v);
	}

	@Override
	public void saveTo(final Map<String, String> config, final String name) {
		if (getter == null) {
			return;
		}

		final T value = getter.get();
		if (defaultValue == value) {
			return;
		}

		config.put(name, value.name());
	}

}
