package log.charter.io.gp.gp5.data;

import java.util.ArrayList;
import java.util.List;

import log.charter.song.enums.BassPickingTechnique;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;

public class GPBeatEffects {
	public final boolean vibrato;
	public final boolean rasgueado;
	public final HOPO hopo;
	public final BassPickingTechnique bassPickingTechnique;
	public final List<GPBend> tremoloEffects;
	public final Harmonic harmonic;

	public GPBeatEffects(final boolean vibrato, final boolean rasgueado, final HOPO hopo,
			final BassPickingTechnique bassPickingTechnique, final List<GPBend> tremoloEffects,
			final Harmonic harmonic) {
		this.vibrato = vibrato;
		this.rasgueado = rasgueado;
		this.hopo = hopo;
		this.bassPickingTechnique = bassPickingTechnique;
		this.tremoloEffects = tremoloEffects;
		this.harmonic = harmonic;
	}

	public GPBeatEffects() {
		this(false, false, HOPO.NONE, BassPickingTechnique.NONE, new ArrayList<>(), Harmonic.NONE);
	}

	@Override
	public String toString() {
		return "GPBeatEffects [vibrato=" + vibrato + ", rasgueado=" + rasgueado + ", hopo=" + hopo
				+ ", bassPickingTechnique=" + bassPickingTechnique + ", tremoloEffects=" + tremoloEffects
				+ ", harmonic=" + harmonic + "]";
	}
}
