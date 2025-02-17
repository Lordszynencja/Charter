package log.charter.data.config.values;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ValueAccessor {
	public static ValueAccessor empty = new ValueAccessor(s -> {}, null);

	public static ValueAccessor forString(final Consumer<String> setter, final Supplier<String> getter) {
		return new ValueAccessor(setter, getter);
	}

	public static ValueAccessor forBoolean(final Consumer<Boolean> setter, final Supplier<Boolean> getter) {
		return new ValueAccessor(s -> setter.accept(Boolean.valueOf(s)),
				getter == null ? null : () -> getter.get() + "");
	}

	public static ValueAccessor forInteger(final Consumer<Integer> setter, final Supplier<Integer> getter) {
		return new ValueAccessor(s -> setter.accept(Integer.valueOf(s)),
				getter == null ? null : () -> getter.get() + "");
	}

	public static ValueAccessor forFloat(final Consumer<Float> setter, final Supplier<Float> getter) {
		return new ValueAccessor(s -> setter.accept(Float.valueOf(s)), getter == null ? null : () -> getter.get() + "");
	}

	public static ValueAccessor forDouble(final Consumer<Double> setter, final Supplier<Double> getter) {
		return new ValueAccessor(s -> setter.accept(Double.valueOf(s)),
				getter == null ? null : () -> getter.get() + "");
	}

	private final Consumer<String> setter;
	private final Supplier<String> getter;

	private ValueAccessor(final Consumer<String> setter, final Supplier<String> getter) {
		this.setter = setter;
		this.getter = getter;
	}

	public void set(final String value) {
		setter.accept(value);
	}

	public String get() {
		return getter.get();
	}

	/**
	 * @return true if value was only supposed to be read from a file, not saved
	 */
	public boolean hasGetter() {
		return getter != null;
	}
}