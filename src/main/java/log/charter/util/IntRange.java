package log.charter.util;

public class IntRange {
	public final int min;
	public final int max;

	public IntRange(final int min, final int max) {
		if (min < max) {
			this.min = min;
			this.max = max;
		} else {
			this.min = max;
			this.max = min;
		}
	}

	public IntRange extend(final int newValue) {
		final int newMin = Math.min(min, newValue);
		final int newMax = Math.max(max, newValue);
		return new IntRange(newMin, newMax);
	}

	public boolean inRange(final int value) {
		return value >= min && value <= max;
	}

	@Override
	public String toString() {
		return "IntRange [min=" + min + ", max=" + max + "]";
	}
}
