package log.charter.data.config.values.accessors;

import java.util.Map;
import java.util.function.BooleanSupplier;

public class BooleanValueAccessor implements ValueAccessor {
	public static BooleanValueAccessor forBoolean(final BooleanSetter setter, final BooleanSupplier getter,
			final boolean defaultValue) {
		return new BooleanValueAccessor(setter, getter, defaultValue);
	}

	public static interface BooleanSetter {
		void set(boolean v);
	}

	private final BooleanSetter setter;
	private final BooleanSupplier getter;
	private final boolean defaultValue;

	private BooleanValueAccessor(final BooleanSetter setter, final BooleanSupplier getter, final boolean defaultValue) {
		this.setter = setter;
		this.getter = getter;
		this.defaultValue = defaultValue;
	}

	@Override
	public void set(final String value) {
		final boolean v = "true".equals(value) ? true : "false".equals(value) ? false : defaultValue;
		setter.set(v);
	}

	@Override
	public void saveTo(final Map<String, String> config, final String name) {
		if (getter == null) {
			return;
		}

		final boolean value = getter.getAsBoolean();
		if (value == defaultValue) {
			return;
		}

		config.put(name, value + "");
	}

}
