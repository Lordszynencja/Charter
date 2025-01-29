package log.charter.util.data;

public class TimeSignature {
	public final int numerator;
	public final int denominator;

	public TimeSignature(final int numerator, final int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}
}
