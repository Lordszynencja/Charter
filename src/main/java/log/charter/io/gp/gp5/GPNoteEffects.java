package log.charter.io.gp.gp5;

import java.util.ArrayList;
import java.util.List;

import log.charter.song.enums.Harmonic;

public class GPNoteEffects {
	public final List<GPBend> bends;
	public final GPGraceNote graceNote;
	public final GPDuration tremoloPickingSpeed;
	public final GPSlideType slideOut;
	public final GPSlideType slideIn;
	public final Harmonic harmonic;
	public final GPTrill trill;
	public final boolean isHammerPullOrigin;
	public final boolean vibrato;
	public final boolean staccato;
	public final boolean palmMute;

	public GPNoteEffects(final List<GPBend> bends, final GPGraceNote graceNote, final GPDuration tremoloPickingSpeed,
			final GPSlideType slideOut, final GPSlideType slideIn, final Harmonic harmonic, final GPTrill trill,
			final boolean isHammerPullOrigin, final boolean vibrato, final boolean staccato, final boolean palmMute) {
		this.bends = bends;
		this.graceNote = graceNote;
		this.tremoloPickingSpeed = tremoloPickingSpeed;
		this.slideOut = slideOut;
		this.slideIn = slideIn;
		this.harmonic = harmonic;
		this.trill = trill;
		this.isHammerPullOrigin = isHammerPullOrigin;
		this.vibrato = vibrato;
		this.staccato = staccato;
		this.palmMute = palmMute;
	}

	public GPNoteEffects() {
		this(new ArrayList<>(), null, null, null, null, null, null, false, false, false, false);
	}

	@Override
	public String toString() {
		return "GPNoteEffects [bends=" + bends + ", graceNote=" + graceNote + ", tremoloPickingSpeed="
				+ tremoloPickingSpeed + ", slideOut=" + slideOut + ", slideIn=" + slideIn + ", harmonic=" + harmonic
				+ ", trill=" + trill + ", isHammerPullOrigin=" + isHammerPullOrigin + ", vibrato=" + vibrato
				+ ", staccato=" + staccato + ", palmMute=" + palmMute + "]";
	}

}
