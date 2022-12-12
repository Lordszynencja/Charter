package log.charter.util;

public class IntRange {
	public final int min;
	public final int max;

	public IntRange(final int a, final int b) {
		if (a < b) {
			min = a;
			max = b;
		} else {
			min = b;
			max = a;
		}
	}
}
