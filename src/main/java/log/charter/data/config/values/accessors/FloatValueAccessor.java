package log.charter.data.config.values.accessors;

import java.util.Map;

public class FloatValueAccessor implements ValueAccessor {
	public static FloatValueAccessor forFloat(final FloatConsumer setter, final FloatSupplier getter,
			final float defaultValue) {
		return new FloatValueAccessor(setter, getter, defaultValue);
	}

	public interface FloatConsumer {
		void accept(float v);
	}

	public interface FloatSupplier {
		float get();
	}

	private final FloatConsumer setter;
	private final FloatSupplier getter;
	private final float defaultValue;

	private FloatValueAccessor(final FloatConsumer setter, final FloatSupplier getter, final float defaultValue) {
		this.setter = setter;
		this.getter = getter;
		this.defaultValue = defaultValue;
	}

	@Override
	public void set(final String value) {
		float v;
		try {
			v = Float.valueOf(value);
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

		final float value = getter.get();
		if (value == defaultValue) {
			return;
		}

		config.put(name, value + "");
	}

}
