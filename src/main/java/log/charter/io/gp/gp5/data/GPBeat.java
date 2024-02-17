package log.charter.io.gp.gp5.data;

import java.util.List;

public class GPBeat {
	public final int tempo;
	public final int dots;
	public final boolean isEmpty;
	public final GPDuration duration;
	public final int tupletNumerator;
	public final int tupletDenominator;
	public final GPBeatEffects beatEffects;
	public final GPChord chord;
	public final List<GPNote> notes;
	public final String text;

	public GPBeat(final int tempo, final int dots, final boolean isEmpty, final GPDuration duration,
			final int tupletNumerator, final int tupletDenominator, final GPBeatEffects beatEffects,
			final GPChord chord, final List<GPNote> notes, final String text) {
		this.tempo = tempo;
		this.dots = dots;
		this.isEmpty = isEmpty;
		this.duration = duration;
		this.tupletNumerator = tupletNumerator;
		this.tupletDenominator = tupletDenominator;
		this.beatEffects = beatEffects;
		this.chord = chord;
		this.notes = notes;
		this.text = text;
	}

	@Override
	public String toString() {
		return "GPBeat [tempo=" + tempo + ", dots=" + dots + ", isEmpty=" + isEmpty + ", duration=" + duration
				+ ", tupletNumerator=" + tupletNumerator + ", tupletDenominator=" + tupletDenominator + ", beatEffects="
				+ beatEffects + ", chord=" + chord + ", notes=" + notes + ", text=" + text + "]";
	}

}
