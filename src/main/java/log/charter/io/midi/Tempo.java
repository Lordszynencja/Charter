package log.charter.io.midi;

public class Tempo {

	public static Tempo forTempoChange(final int id, final int kbpm) {
		return new Tempo(id, id, kbpm, -1, -1);
	}

	public static Tempo forTSChange(final int id, final int numerator, final int denominator) {
		return new Tempo(id, id, -1, numerator, denominator);
	}

	public double pos;
	public int id;
	public int kbpm;
	public int numerator;
	public int denominator;

	public Tempo() {
		this(0, 0, 120_000, 4, 4);
	}

	public Tempo(final double pos, final int id, final int kbpm, final int numerator, final int denominator) {
		this.pos = pos;
		this.id = id;
		this.kbpm = kbpm;
		this.numerator = numerator;
		this.denominator = denominator;
	}
}
