package log.charter.data.config.values.accessors;

import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class DoubleValueAccessor implements ValueAccessor {
	public static DoubleValueAccessor forDouble(final DoubleConsumer setter, final DoubleSupplier getter,
			final double defaultValue) {
		return new DoubleValueAccessor(setter, getter, defaultValue);
	}

	private final DoubleConsumer setter;
	private final DoubleSupplier getter;
	private final double defaultValue;

	private DoubleValueAccessor(final DoubleConsumer setter, final DoubleSupplier getter, final double defaultValue) {
		this.setter = setter;
		this.getter = getter;
		this.defaultValue = defaultValue;
	}

	@Override
	public void set(final String value) {
		double v;
		try {
			v = Double.valueOf(value);
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

		final double value = getter.getAsDouble();
		if (value == defaultValue) {
			return;
		}

		config.put(name, value + "");
	}

}
