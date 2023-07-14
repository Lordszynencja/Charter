package log.charter.io.gp.gp5;

public class GPGraceNote {
	public final int fret;
	public final GPDuration duration;
	public final boolean slide;
	public final boolean beforeBeat;
	public final boolean dead;

	public GPGraceNote(final int fret, final GPDuration duration, final boolean slide, final boolean beforeBeat,
			final boolean dead) {
		this.fret = fret;
		this.duration = duration;
		this.slide = slide;
		this.beforeBeat = beforeBeat;
		this.dead = dead;
	}

	@Override
	public String toString() {
		return "GPGraceNote [fret=" + fret + ", duration=" + duration + ", slide=" + slide + ", beforeBeat="
				+ beforeBeat + ", dead=" + dead + "]";
	}

}
