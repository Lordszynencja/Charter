package log.charter.util.data;

import static java.lang.Math.abs;

public class Fraction implements Comparable<Fraction> {
	private static long gcd(final long a, final long b) {
		if (b > a) {
			return gcd(b, a);
		}
		if (b == 0) {
			return 1;
		}
		if (a == b) {
			return a;
		}

		return gcd(a - b, b);
	}

	public final long numerator;
	public final long denominator;

	public Fraction(long numerator, long denominator) {
		if (denominator < 0) {
			numerator = -numerator;
			denominator = -denominator;
		}

		final long gcd = gcd(abs(numerator), abs(denominator));
		this.numerator = numerator / gcd;
		this.denominator = denominator / gcd;
	}

	public Fraction negative() {
		return new Fraction(-numerator, denominator);
	}

	public Fraction add(final long number) {
		return new Fraction(numerator + denominator * number, denominator);
	}

	public Fraction add(final Fraction other) {
		if (other.denominator == denominator) {
			return new Fraction(numerator + other.numerator, denominator);
		}

		final long gcd = gcd(denominator, other.denominator);
		final long newNumerator = (numerator * other.denominator + other.numerator * denominator) / gcd;
		final long newDenominator = denominator * other.denominator / gcd;
		return new Fraction(newNumerator, newDenominator);
	}

	public Fraction add(final long numerator, final long denominator) {
		return add(new Fraction(numerator, denominator));
	}

	public Fraction multiply(final Fraction other) {
		return new Fraction(numerator * other.numerator, denominator * other.denominator);
	}

	public Fraction multiply(final int numerator, final int denominator) {
		return multiply(new Fraction(numerator, denominator));
	}

	public Fraction multiply(final int number) {
		return new Fraction(numerator * number, denominator);
	}

	public Fraction divide(final int number) {
		return new Fraction(numerator, denominator * number);
	}

	public int intValue() {
		return (int) (numerator / denominator);
	}

	public double doubleValue() {
		return (double) numerator / denominator;
	}

	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}

	@Override
	public int compareTo(final Fraction o) {
		final long difference = this.add(o.negative()).numerator;
		return difference > 0 ? 1//
				: difference < 0 ? -1//
						: 0;
	}
}
