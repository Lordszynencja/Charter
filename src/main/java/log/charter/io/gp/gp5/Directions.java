package log.charter.io.gp.gp5;

public class Directions {
	public int coda;
	public int double_coda;
	public int segno;
	public int segno_segno;
	public int fine;
	public int da_capo;
	public int da_capo_al_coda;
	public int da_capo_al_double_coda;
	public int da_capo_al_fine;
	public int da_segno;
	public int da_segno_al_coda;
	public int da_segno_al_double_coda;
	public int da_segno_al_fine;
	public int da_segno_segno;
	public int da_segno_segno_al_coda;
	public int da_segno_segno_al_double_coda;
	public int da_segno_segno_al_fine;
	public int da_coda;
	public int da_double_coda;

	public Directions() {
		coda = 0;
		double_coda = 0;
		segno = 0;
		segno_segno = 0;
		fine = 0;
		da_capo = 0;
		da_capo_al_coda = 0;
		da_capo_al_double_coda = 0;
		da_capo_al_fine = 0;
		da_segno = 0;
		da_segno_al_coda = 0;
		da_segno_al_double_coda = 0;
		da_segno_al_fine = 0;
		da_segno_segno = 0;
		da_segno_segno_al_coda = 0;
		da_segno_segno_al_double_coda = 0;
		da_segno_segno_al_fine = 0;
		da_coda = 0;
		da_double_coda = 0;
	}

	@Override
	public String toString() {
		return "Directions [" +
			   "coda=" + coda +
			   ", double_coda=" + double_coda +
			   ", segno=" + segno +
			   ", segno_segno=" + segno_segno +
			   ", fine=" + fine +
			   ", da_capo=" + da_capo +
			   ", da_capo_al_coda=" + da_capo_al_coda +
			   ", da_capo_al_double_coda=" + da_capo_al_double_coda +
			   ", da_capo_al_fine=" + da_capo_al_fine +
			   ", da_segno=" + da_segno +
			   ", da_segno_al_coda=" + da_segno_al_coda +
			   ", da_segno_al_double_coda=" + da_segno_al_double_coda +
			   ", da_segno_al_fine=" + da_segno_al_fine +
			   ", da_segno_segno=" + da_segno_segno +
			   ", da_segno_segno_al_coda=" + da_segno_segno_al_coda +
			   ", da_segno_segno_al_double_coda=" + da_segno_segno_al_double_coda +
			   ", da_segno_segno_al_fine=" + da_segno_segno_al_fine +
			   ", da_coda=" + da_coda +
			   ", da_double_coda=" + da_double_coda +
			   "]";
	}

}
