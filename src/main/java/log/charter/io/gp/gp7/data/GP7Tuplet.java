package log.charter.io.gp.gp7.data;

public class GP7Tuplet {
	public int numerator;
	public int denominator;

	public GP7Tuplet() {
		this(1, 1);
	}

	public GP7Tuplet(final int numerator, final int denominator) {
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override
	public String toString() {
		return "GP7Tuplet [numerator=" + numerator + ", denominator=" + denominator + "]";
	}

	public boolean isSimple() {
		return numerator == denominator;
	}
}
