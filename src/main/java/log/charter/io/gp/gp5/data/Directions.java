package log.charter.io.gp.gp5.data;

public class Directions {
	public int coda;
	public int doubleCoda;
	public int segno;
	public int segnoSegno;
	public int fine;
	public int daCapo;
	public int daCapoAlCoda;
	public int daCapoAlDoubleCoda;
	public int daCapoAlFine;
	public int daSegno;
	public int daSegnoAlCoda;
	public int daSegnoAlDoubleCoda;
	public int daSegnoAlFine;
	public int daSegnoSegno;
	public int daSegnoSegnoAlCoda;
	public int daSegnoSegnoAlDoubleCoda;
	public int daSegnoSegnoAlFine;
	public int daCoda;
	public int daDoubleCoda;

	public Directions() {
		coda = 0;
		doubleCoda = 0;
		segno = 0;
		segnoSegno = 0;
		fine = 0;
		daCapo = 0;
		daCapoAlCoda = 0;
		daCapoAlDoubleCoda = 0;
		daCapoAlFine = 0;
		daSegno = 0;
		daSegnoAlCoda = 0;
		daSegnoAlDoubleCoda = 0;
		daSegnoAlFine = 0;
		daSegnoSegno = 0;
		daSegnoSegnoAlCoda = 0;
		daSegnoSegnoAlDoubleCoda = 0;
		daSegnoSegnoAlFine = 0;
		daCoda = 0;
		daDoubleCoda = 0;
	}

	public Directions(final Directions other) {
		coda = other.coda;
		doubleCoda = other.doubleCoda;
		segno = other.segno;
		segnoSegno = other.segnoSegno;
		fine = other.fine;
		daCapo = other.daCapo;
		daCapoAlCoda = other.daCapoAlCoda;
		daCapoAlDoubleCoda = other.daCapoAlDoubleCoda;
		daCapoAlFine = other.daCapoAlFine;
		daSegno = other.daSegno;
		daSegnoAlCoda = other.daSegnoAlCoda;
		daSegnoAlDoubleCoda = other.daSegnoAlDoubleCoda;
		daSegnoAlFine = other.daSegnoAlFine;
		daSegnoSegno = other.daSegnoSegno;
		daSegnoSegnoAlCoda = other.daSegnoSegnoAlCoda;
		daSegnoSegnoAlDoubleCoda = other.daSegnoSegnoAlDoubleCoda;
		daSegnoSegnoAlFine = other.daSegnoSegnoAlFine;
		daCoda = other.daCoda;
		daDoubleCoda = other.daDoubleCoda;
	}

	@Override
	public String toString() {
		return "Directions [" + "coda=" + coda + ", doubleCoda=" + doubleCoda + ", segno=" + segno + ", segnoSegno="
				+ segnoSegno + ", fine=" + fine + ", daCapo=" + daCapo + ", daCapoAlCoda=" + daCapoAlCoda
				+ ", daCapoAlDoubleCoda=" + daCapoAlDoubleCoda + ", daCapoAlFine=" + daCapoAlFine + ", daSegno="
				+ daSegno + ", daSegnoAlCoda=" + daSegnoAlCoda + ", daSegnoAlDoubleCoda=" + daSegnoAlDoubleCoda
				+ ", daSegnoAlFine=" + daSegnoAlFine + ", daSegnoSegno=" + daSegnoSegno + ", daSegnoSegnoAlCoda="
				+ daSegnoSegnoAlCoda + ", daSegnoSegnoAlDoubleCoda=" + daSegnoSegnoAlDoubleCoda
				+ ", daSegnoSegnoAlFine=" + daSegnoSegnoAlFine + ", daCoda=" + daCoda + ", daDoubleCoda=" + daDoubleCoda
				+ "]";
	}

}
