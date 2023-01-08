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
}
