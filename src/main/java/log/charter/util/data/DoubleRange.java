package log.charter.util.data;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class DoubleRange {
	public final double min;
	public final double max;

	public DoubleRange(final double min, final double max) {
		if (min < max) {
			this.min = min;
			this.max = max;
		} else {
			this.min = max;
			this.max = min;
		}
	}

	public DoubleRange extend(final double newValue) {
		final double newMin = min(min, newValue);
		final double newMax = max(max, newValue);
		return new DoubleRange(newMin, newMax);
	}

	public boolean inRange(final double value) {
		return value >= min && value <= max;
	}

	@Override
	public String toString() {
		return "DoubleRange [min=" + min + ", max=" + max + "]";
	}
}
