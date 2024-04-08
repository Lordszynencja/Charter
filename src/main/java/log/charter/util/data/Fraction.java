package log.charter.util.data;

import static java.lang.Math.abs;

import java.util.Objects;

public class Fraction implements Comparable<Fraction> {
	private static long gcd(long a, long b) {
		if (a == 0) {
			return b;
		}
		if (b == 0) {
			return a;
		}

		a = abs(a);
		b = abs(b);
		while (b != 0) {
			a -= b * (a / b);

			final long tmp = b;
			b = a;
			a = tmp;
		}

		return a;
	}

	public static Fraction fromString(final String value) {
		final String[] tokens = value.split("/");
		return new Fraction(Long.valueOf(tokens[0]), Long.valueOf(tokens[1]));
	}

	public final long numerator;
	public final long denominator;

	public Fraction(final long number) {
		numerator = number;
		denominator = 1;
	}

	public Fraction(long numerator, long denominator) {
		if (denominator == 0) {
			throw new IllegalArgumentException("can't have fraction with denominator 0");
		}

		if (numerator == 0) {
			this.numerator = 0;
			this.denominator = 1;
			return;
		}

		if (denominator < 0) {
			numerator = -numerator;
			denominator = -denominator;
		}

		final long gcd = gcd(abs(numerator), abs(denominator));
		this.numerator = numerator / gcd;
		this.denominator = denominator / gcd;
	}

	public Fraction negate() {
		return new Fraction(-numerator, denominator);
	}

	public Fraction absolute() {
		return numerator >= 0 ? this : negate();
	}

	public boolean negative() {
		return numerator < 0 != denominator < 0;
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

	public float floatValue() {
		return (float) numerator / denominator;
	}

	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}

	public String asString() {
		return numerator + "/" + denominator;
	}

	@Override
	public int compareTo(final Fraction o) {
		final long difference = this.add(o.negate()).numerator;
		return difference > 0 ? 1//
				: difference < 0 ? -1//
						: 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(denominator, numerator);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Fraction other = (Fraction) obj;
		return denominator == other.denominator && numerator == other.numerator;
	}

}
