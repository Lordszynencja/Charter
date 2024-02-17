package log.charter.io.gp.gp5.data;

public class GPChord {
	public final String chordName;
	public final int firstFret;
	public final int[] chordFrets;
	public final byte[] barreFrets;

	public GPChord(final String chordName, final int firstFret, final int[] chordFrets, final byte[] barreFrets) {
		this.chordName = chordName;
		this.firstFret = firstFret;
		this.chordFrets = chordFrets;
		this.barreFrets = barreFrets;
	}

}
