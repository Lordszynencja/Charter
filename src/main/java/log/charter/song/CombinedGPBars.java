package log.charter.song;

import java.util.List;

import log.charter.io.gp.gp5.GPMasterBar;
import log.charter.io.gp.gp5.GPNote;
import log.charter.io.Logger;
import log.charter.io.gp.gp5.GPBeat;
import log.charter.io.gp.gp5.GPBeatEffects;
import log.charter.io.gp.gp5.GPChord;
import log.charter.io.gp.gp5.GPDuration;
import log.charter.util.CollectionUtils.ArrayList2;

public class CombinedGPBars {
	public class GPBeatUnwrapper extends GPBeat {
		private double noteDuration;
		private double noteTimeMs;

		public GPBeatUnwrapper(final int tempo, final int dots, final boolean isEmpty, final GPDuration duration,
				final int tupletNumerator, final int tupletDenominator, final GPBeatEffects beatEffects,
				final GPChord chord, final List<GPNote> notes, final String text) {
			super(tempo, dots, isEmpty, duration, tupletNumerator, tupletDenominator, beatEffects, chord, notes, text);
			this.noteDuration = noteDuration(gpDurationToNoteDenominator(duration), tupletNumerator, tupletDenominator, dots != 0);
			this.noteTimeMs = noteTimeFromDuration(tempo);
		}

		public GPBeatUnwrapper(final GPBeat beat) {
			super(beat.tempo, beat.dots, beat.isEmpty, beat.duration, beat.tupletNumerator, beat.tupletDenominator,
					beat.beatEffects, beat.chord, beat.notes, beat.text);
			this.noteDuration = noteDuration(gpDurationToNoteDenominator(duration), tupletNumerator, tupletDenominator, dots != 0);
			this.noteTimeMs = noteTimeFromDuration(tempo);
		}
		
		public final double getNoteTimeMs() {
			return this.noteTimeMs;
		}

		private final int gpDurationToNoteDenominator(GPDuration duration) {
			// Example length 16 -> 64/16 = 4 -> 1/4 (quarter)
			return (64/duration.length);
		}

		final double gpDurationToTime(GPDuration duration) {
			// Example length 16 -> 64/16 = 4 -> 1/4 (quarter)
			final double calculatedNoteDuration = noteDuration(gpDurationToNoteDenominator(duration), this.tupletNumerator, this.tupletDenominator, this.dots != 0);
			final double calculatedNoteTimeMs = noteTimeFromCustomDuration(this.tempo, calculatedNoteDuration);
			return calculatedNoteTimeMs;
		}

		static final double fourOverFourBeatLength(int tempo) {
			return 60000/(double)tempo;
		}

		double noteTimeFromDuration(int tempo) {
			final double fourOverFourBeatLength = fourOverFourBeatLength(tempo); // Same as whole note note time
			final double calculatedNoteTimeMs = fourOverFourBeatLength * this.noteDuration;
			return calculatedNoteTimeMs;
		}

		double noteTimeFromCustomDuration(int tempo, double duration) {
			final double fourOverFourBeatLength = fourOverFourBeatLength(tempo); // Same as whole note note time
			final double calculatedNoteTimeMs = fourOverFourBeatLength * duration;
			return calculatedNoteTimeMs;
		}

		double noteTime(int tempo, int noteLengthDen, int noteTupleNum, int noteTupleDen, boolean isDottedNote) {
			final double fourOverFourBeatLength = fourOverFourBeatLength(tempo); // Same as whole note note time
			final double calculatedNoteDuration = noteDuration(noteLengthDen, noteTupleNum, noteTupleDen, isDottedNote);
			final double calculatedNoteTimeMs = (int)(fourOverFourBeatLength * calculatedNoteDuration);
			return calculatedNoteTimeMs;
		}

		double noteDuration(int noteLengthDen, int noteTupleNum, int noteTupleDen, boolean isDottedNote) {
			// Example triplet 16th note: 4/16 -> 1/4 (0.25) -> 0.1667 
			final double wholeNoteLength = 4;
			final double calculatedNoteDuration = wholeNoteLength / (double)noteLengthDen;
			double noteTupledLength = calculatedNoteDuration * ((double)noteTupleDen / (double)noteTupleNum);
			if (isDottedNote) {
				noteTupledLength *= 1.5;
			}
			return noteTupledLength;
		}

		static double barDuration(int num, int den) {
			// Example 6/8 -> 3/4, 2/2 -> 4/4, 12/8 -> 6/4
			final double relationToFourDen = (double)den / 4;
			final double relationToFourNum = (double)num / relationToFourDen;
			return relationToFourNum;
		}

		static double barTime(int tempo, int num, int den) {
			double baseBarLength = fourOverFourBeatLength(tempo);
			double barDuration = barDuration(num, den);
			return (baseBarLength * barDuration) / 4;
		}
		static double barDurationToTime(double duration, int tempo) {
			return (duration * barTime(tempo,4,4));
		}
	}

	public class BeatUnwrapper extends Beat {
		double barWidthInTimeMs;
		double floatPos;

		public BeatUnwrapper(final Beat beat) {
			super(beat);
		}

		public BeatUnwrapper(final BeatUnwrapper beat) {
			super(beat);
			this.barWidthInTimeMs = beat.barWidthInTimeMs;
			this.floatPos = beat.floatPos;
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
		this.gpBarId = id;
		this.barBeats = new ArrayList2<>();
		this.noteBeats = new ArrayList2<>();
	}

	public CombinedGPBars(final CombinedGPBars comboBar) {
		this.bar = comboBar.bar;
		this.gpBarId = comboBar.gpBarId;
		this.availableSpaceIn_64ths = comboBar.availableSpaceIn_64ths;
		this.notesInBar = comboBar.notesInBar;
		this.barBeats = new ArrayList2<>();
		for (BeatUnwrapper beat : comboBar.barBeats) {
			barBeats.add(new BeatUnwrapper(beat));
		}
		this.noteBeats = new ArrayList2<>();
		for (GPBeatUnwrapper beat : comboBar.noteBeats) {
			noteBeats.add(new GPBeatUnwrapper(beat));
		}
	}

	public void updateBarsFromNoteTempo(final int previousTempo) {
		double sumOfNoteLengths = 0;
		double sumOfNoteDurations = 0;

		int beatIndex = 0;
		int latestTempo = previousTempo;
		double beatDuration = 4.0 / this.barBeats.get(beatIndex).noteDenominator;
		double durationToExtractPosition = beatDuration;

		this.barBeats.get(beatIndex).position(0);
		this.barBeats.get(beatIndex).firstInMeasure = true;

		beatIndex++;

		for (final GPBeatUnwrapper noteBeat : this.noteBeats) {
			sumOfNoteLengths += noteBeat.noteTimeMs;
			sumOfNoteDurations += noteBeat.noteDuration;

			if ((int)sumOfNoteDurations * 16 > this.availableSpaceIn_64ths) {
				Logger.error("Bar exceeds allowed duration. Bar: " + this.gpBarId);
				return;
			}
			
			if (noteBeat.tempo != latestTempo) {
				this.barBeats.get(beatIndex-1).anchor = true;
			}
			// While loop to handle long notes over multiple beats
			while (sumOfNoteDurations >= durationToExtractPosition) {
				double overshootDuration = sumOfNoteDurations - durationToExtractPosition;
				double overshootTime = 0;

				if (overshootDuration > 0) {
					overshootTime = (overshootDuration * noteBeat.noteTimeMs) / noteBeat.noteDuration;
				}

				if (beatIndex < this.barBeats.size()) {
					this.barBeats.get(beatIndex).position((int)(sumOfNoteLengths-overshootTime));
					this.barBeats.get(beatIndex).floatPos = sumOfNoteLengths-overshootTime;
					this.barBeats.get(beatIndex).firstInMeasure = false;
				}
				if (beatIndex > 0) {
					this.barBeats.get(beatIndex-1).barWidthInTimeMs = sumOfNoteLengths - overshootTime - this.barBeats.get(beatIndex-1).floatPos;
				}
				beatIndex++;
				durationToExtractPosition += beatDuration;
			}
			latestTempo = noteBeat.tempo;
		}
	}
}
