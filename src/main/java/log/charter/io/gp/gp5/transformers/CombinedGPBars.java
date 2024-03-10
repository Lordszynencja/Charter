package log.charter.io.gp.gp5.transformers;

import java.util.List;

import log.charter.data.song.Beat;
import log.charter.io.Logger;
import log.charter.io.gp.gp5.data.GPBeat;
import log.charter.io.gp.gp5.data.GPBeatEffects;
import log.charter.io.gp.gp5.data.GPChord;
import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.io.gp.gp5.data.GPMasterBar;
import log.charter.io.gp.gp5.data.GPNote;
import log.charter.util.CollectionUtils.ArrayList2;

public class CombinedGPBars {
	public static class GPBeatUnwrapper extends GPBeat {
		private final double noteDuration;
		private final double noteTimeMs;

		public GPBeatUnwrapper(final int tempo, final int dots, final boolean isEmpty, final GPDuration duration,
				final int tupletNumerator, final int tupletDenominator, final GPBeatEffects beatEffects,
				final GPChord chord, final List<GPNote> notes, final String text) {
			super(tempo, dots, isEmpty, duration, tupletNumerator, tupletDenominator, beatEffects, chord, notes, text);
			noteDuration = noteDuration(gpDurationToNoteDenominator(duration), tupletNumerator, tupletDenominator,
					dots != 0);
			noteTimeMs = noteTimeFromDuration(tempo);
		}

		public GPBeatUnwrapper(final GPBeat beat) {
			super(beat.tempo, beat.dots, beat.isEmpty, beat.duration, beat.tupletNumerator, beat.tupletDenominator,
					beat.beatEffects, beat.chord, beat.notes, beat.text);
			noteDuration = noteDuration(gpDurationToNoteDenominator(duration), tupletNumerator, tupletDenominator,
					dots != 0);
			noteTimeMs = noteTimeFromDuration(tempo);
		}

		public final double getNoteTimeMs() {
			return noteTimeMs;
		}

		private final int gpDurationToNoteDenominator(final GPDuration duration) {
			return 64 / duration.length;
		}

		public final double gpDurationToTime(final GPDuration duration) {
			final double calculatedNoteDuration = noteDuration(gpDurationToNoteDenominator(duration), tupletNumerator,
					tupletDenominator, dots != 0);
			final double calculatedNoteTimeMs = noteTimeFromCustomDuration(tempo, calculatedNoteDuration);
			return calculatedNoteTimeMs;
		}

		static final double fourOverFourBeatLength(final int tempo) {
			return 60000 / (double) tempo;
		}

		private double noteTimeFromDuration(final int tempo) {
			final double fourOverFourBeatLength = fourOverFourBeatLength(tempo); // Same as whole note note time
			final double calculatedNoteTimeMs = fourOverFourBeatLength * noteDuration;
			return calculatedNoteTimeMs;
		}

		private double noteTimeFromCustomDuration(final int tempo, final double duration) {
			final double fourOverFourBeatLength = fourOverFourBeatLength(tempo); // Same as whole note note time
			final double calculatedNoteTimeMs = fourOverFourBeatLength * duration;
			return calculatedNoteTimeMs;
		}

		private double noteDuration(final int noteLengthDen, final int noteTupleNum, final int noteTupleDen,
				final boolean isDottedNote) {
			final double calculatedNoteDuration = 4.0 / noteLengthDen;
			double noteTupledLength = calculatedNoteDuration * noteTupleDen / noteTupleNum;
			if (isDottedNote) {
				noteTupledLength *= 1.5;
			}

			return noteTupledLength;
		}
	}

	public static class BeatUnwrapper extends Beat {
		double barWidthInTimeMs;
		double floatPos;

		public BeatUnwrapper(final Beat beat) {
			super(beat);
		}

		public BeatUnwrapper(final BeatUnwrapper beat) {
			super(beat);
			barWidthInTimeMs = beat.barWidthInTimeMs;
			floatPos = beat.floatPos;
		}
	}

	public GPMasterBar bar;
	public ArrayList2<BeatUnwrapper> barBeats;
	public ArrayList2<GPBeatUnwrapper> noteBeats;
	public int gpBarId;
	public int availableSpaceIn_64ths;
	public int notesInBar;

	public CombinedGPBars(final GPMasterBar bar, final int id) {
		this.bar = bar;
		gpBarId = id;
		barBeats = new ArrayList2<>();
		noteBeats = new ArrayList2<>();
	}

	public CombinedGPBars(final CombinedGPBars comboBar) {
		bar = comboBar.bar;
		gpBarId = comboBar.gpBarId;
		availableSpaceIn_64ths = comboBar.availableSpaceIn_64ths;
		notesInBar = comboBar.notesInBar;
		barBeats = new ArrayList2<>();
		for (final BeatUnwrapper beat : comboBar.barBeats) {
			barBeats.add(new BeatUnwrapper(beat));
		}
		noteBeats = new ArrayList2<>();
		for (final GPBeatUnwrapper beat : comboBar.noteBeats) {
			noteBeats.add(new GPBeatUnwrapper(beat));
		}
	}

	public void updateBarsFromNoteTempo(final int previousTempo) {
		double sumOfNoteLengths = 0;
		double sumOfNoteDurations = 0;

		int beatIndex = 0;
		int latestTempo = previousTempo;
		final double beatDuration = 4.0 / barBeats.get(beatIndex).noteDenominator;
		double durationToExtractPosition = beatDuration;

		barBeats.get(beatIndex).position(0);
		barBeats.get(beatIndex).firstInMeasure = true;

		beatIndex++;

		for (final GPBeatUnwrapper beatUnwrapper : noteBeats) {
			sumOfNoteLengths += beatUnwrapper.noteTimeMs;
			sumOfNoteDurations += beatUnwrapper.noteDuration;

			if ((int) sumOfNoteDurations * 16 > availableSpaceIn_64ths) {
				Logger.error("Bar exceeds allowed duration. Bar: " + gpBarId);
				return;
			}

			final BeatUnwrapper lastBarBeat = barBeats.get(beatIndex - 1);
			if (beatUnwrapper.tempo != latestTempo) {
				lastBarBeat.anchor = true;
			}
			// While loop to handle long notes over multiple beats
			while (sumOfNoteDurations >= durationToExtractPosition) {
				final double overshootDuration = sumOfNoteDurations - durationToExtractPosition;
				double overshootTime = 0;

				if (overshootDuration > 0) {
					overshootTime = (overshootDuration * beatUnwrapper.noteTimeMs) / beatUnwrapper.noteDuration;
				}

				if (beatIndex < barBeats.size()) {
					final BeatUnwrapper barBeat = barBeats.get(beatIndex);
					barBeat.position((int) (sumOfNoteLengths - overshootTime));
					barBeat.floatPos = sumOfNoteLengths - overshootTime;
					barBeat.firstInMeasure = false;
				}
				lastBarBeat.barWidthInTimeMs = sumOfNoteLengths - overshootTime - barBeats.get(beatIndex - 1).floatPos;

				beatIndex++;
				durationToExtractPosition += beatDuration;
			}
			latestTempo = beatUnwrapper.tempo;
		}
	}
}
