package log.charter.io.gp.gp5;

public class GPNote {
	/**
	 * 1-based
	 */
	public final int string;
	public final int fret;
	public final boolean accent;
	public final boolean ghost;
	public final boolean dead;
	public final boolean tied;
	public final int finger;
	public final double durationPercent;
	public final GPNoteEffects effects;

	public GPNote(final int string, final int fret, final boolean accent, final boolean ghost, final boolean dead,
			final boolean tied, final int finger, final double durationPercent, final GPNoteEffects effects) {
		this.string = string;
		this.fret = fret;
		this.accent = accent;
		this.ghost = ghost;
		this.dead = dead;
		this.tied = tied;
		this.finger = finger;
		this.durationPercent = durationPercent;
		this.effects = effects;
	}

	@Override
	public String toString() {
		return "GPNote [string=" + string + ", fret=" + fret + ", accent=" + accent + ", ghost=" + ghost + ", dead="
				+ dead + ", tied=" + tied + ", finger=" + finger + ", durationPercent=" + durationPercent + ", effects="
				+ effects + "]";
	}

}
