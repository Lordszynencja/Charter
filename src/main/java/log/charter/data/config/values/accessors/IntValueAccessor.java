package log.charter.data.config.values.accessors;

import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntValueAccessor implements ValueAccessor {
	public static IntValueAccessor forInteger(final IntConsumer setter, final IntSupplier getter, final int defaultValue) {
		return new IntValueAccessor(setter, getter, defaultValue);
	}

	private final IntConsumer setter;
	private final IntSupplier getter;
	private final int defaultValue;

	private IntValueAccessor(final IntConsumer setter, final IntSupplier getter, final int defaultValue) {
		this.setter = setter;
		this.getter = getter;
		this.defaultValue = defaultValue;
	}

	@Override
	public void set(final String value) {
		int v;
		try {
			v = Integer.valueOf(value);
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

		final int value = getter.getAsInt();
		if (value == defaultValue) {
			return;
		}

		config.put(name, value + "");
	}

}
