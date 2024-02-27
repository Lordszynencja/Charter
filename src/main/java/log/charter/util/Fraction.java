package log.charter.util;

import static java.lang.Math.abs;

public class Fraction {
	private static int gcd(final int a, final int b) {
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

	public final int numerator;
	public final int denominator;

	public Fraction(final int numerator, final int denominator) {
		final int gcd = gcd(abs(numerator), abs(denominator));
		this.numerator = numerator / gcd;
		this.denominator = denominator / gcd;
	}

	public Fraction add(final int numerator, final int denominator) {
		return add(new Fraction(numerator, denominator));
	}

	public Fraction add(final Fraction other) {
		if (other.denominator == denominator) {
			return new Fraction(numerator + other.numerator, denominator);
		}

		final int gcd = gcd(denominator, other.denominator);
		final int newNumerator = (numerator * other.denominator + other.numerator * denominator) / gcd;
		final int newDenominator = denominator * other.denominator / gcd;
		return new Fraction(newNumerator, newDenominator);
	}

	public Fraction multiply(final int numerator, final int denominator) {
		return multiply(new Fraction(numerator, denominator));
	}

	public Fraction multiply(final Fraction other) {
		return new Fraction(numerator * other.numerator, denominator * other.denominator);
	}

	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}
}
