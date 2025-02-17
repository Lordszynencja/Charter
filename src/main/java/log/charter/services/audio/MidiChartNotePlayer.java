package log.charter.services.audio;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.data.song.notes.ChordOrNote.findNextSoundOnString;
import static log.charter.util.CollectionUtils.lastBeforeEqual;
import static log.charter.util.CollectionUtils.min;
import static log.charter.util.Utils.mix;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.song.BendValue;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

public class MidiChartNotePlayer implements Initiable {
	private static final int minSoundLength = 100;

	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;
	private ModeManager modeManager;

	private final MidiNotePlayer midiNotePlayer = new MidiNotePlayer();

	private boolean playing = false;
	private int speed = 100;
	private final List<MidiChartNotePlayerNoteData> soundsPlaying = new ArrayList<>();
	private MidiChartNotePlayerNoteData nextSoundToPlay;

	@Override
	public void init() {
		midiNotePlayer.init(chartData);
	}

	private double getTime() {
		return chartTimeHandler.time() + Config.audio.midiDelay * speed / 100;
	}

	private void stopSound(final MidiChartNotePlayerNoteData sound) {
		sound.sound.notes().forEach(note -> {
			if (!note.linkNext()) {
				midiNotePlayer.stopSound(note.string());
			}
		});
	}

	private void playNextSound() {
		if (nextSoundToPlay == null || nextSoundToPlay.sound == null) {
			return;
		}

		midiNotePlayer.playSound(nextSoundToPlay.sound);
		soundsPlaying.add(nextSoundToPlay);
		final int newNextSoundId = nextSoundToPlay.noteId + 1;
		final List<ChordOrNote> chartSounds = chartData.currentSounds();
		if (chartSounds.size() > newNextSoundId) {
			nextSoundToPlay = makeNoteData(newNextSoundId);
		} else {
			nextSoundToPlay = null;
		}
	}

	private double getBendValue(final double time, final List<BendValue> bendValues, final FractionalPosition position,
			final FractionalPosition endPosition) {
		if (bendValues.isEmpty()) {
			return 0;
		}

		final Integer bendPointId = lastBeforeEqual(bendValues, FractionalPosition.fromTime(chartData.beats(), time))
				.findId();
		BendValue bendPointA;
		BendValue bendPointB;
		if (bendPointId == null) {
			bendPointA = new BendValue(position, BigDecimal.ZERO);
			bendPointB = bendValues.get(0);
		} else {
			bendPointA = bendValues.get(bendPointId);
			bendPointB = bendPointId + 1 < bendValues.size() ? bendValues.get(bendPointId + 1)
					: new BendValue(endPosition, bendPointA.bendValue);
		}

		final double bendPositionA = bendPointA.position(chartData.beats());
		final double bendPositionB = bendPointB.position(chartData.beats());
		final double bendValueA = bendPointA.bendValue.doubleValue();
		final double bendValueB = bendPointB.bendValue.doubleValue();

		return mix(bendPositionA, bendPositionB, time, bendValueA, bendValueB);
	}

	private double getSlideValue(final double time, final FractionalPosition position,
			final FractionalPosition endPosition, final int slide, final boolean unpitchedSlide) {
		final double start = position.position(chartData.beats());
		final double end = endPosition.position(chartData.beats());
		if (end <= start) {
			return 0;
		}

		final double progress = 1.0 * (time - start) / (end - start);

		final double weight = unpitchedSlide//
				? 1 - sin((1 - progress) * Math.PI / 2)//
				: pow(sin(progress * Math.PI / 2), 3);
		return slide * weight;
	}

	private void updateBend(final double time, final FractionalPosition position, final FractionalPosition endPosition,
			final int string, final List<BendValue> bendValues, final int fret, final Integer slideTo,
			final boolean unpitchedSlide, final boolean vibrato) {
		double bendValue = getBendValue(time, bendValues, position, endPosition);

		if (slideTo != null) {
			bendValue += getSlideValue(time, position, endPosition, slideTo - fret, unpitchedSlide);
		}
		if (vibrato) {
			final FractionalPosition inNotePosition = position
					.distance(FractionalPosition.fromTime(chartData.beats(), time));
			bendValue += (1 + Math.cos(inNotePosition.fraction.doubleValue() * Math.PI * 4)) * 0.25;
		}

		midiNotePlayer.updateBend(string, fret, bendValue);
	}

	private void updateSoundingSounds(final double time) {
		for (final MidiChartNotePlayerNoteData sound : soundsPlaying) {
			if (sound.endPosition < time) {
				stopSound(sound);
				continue;
			}

			sound.sound.notesWithFrets(chartData.currentChordTemplates())//
					.forEach(note -> {
						updateBend(time, note.position(), note.endPosition(), note.string(), note.bendValues(),
								note.fret(), note.slideTo(), note.unpitchedSlide(), note.vibrato());
					});
		}

		soundsPlaying.removeIf(sound -> sound.endPosition < time);
	}

	public void frame() {
		if (!playing || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		final double time = getTime();

		midiNotePlayer.updateVolume();
		updateSoundingSounds(time);

		if (nextSoundToPlay != null && nextSoundToPlay.sound.position(chartData.beats()) <= time) {
			playNextSound();
		}
	}

	private static class SoundEndCalculationData {
		public double position;
		public double endPosition;
		public double maxEndTime;
	}

	private void calculateEndForChord(final ChordOrNote sound, final SoundEndCalculationData endCalculationData,
			final ChordOrNote nextSound) {
		if (!sound.isChord() || endCalculationData.endPosition - endCalculationData.position > 0) {
			return;
		}

		final List<Double> possibleEndTimes = new ArrayList<>();
		if (nextSound != null) {
			final double endPositionBeforeNextSound = nextSound.position(chartData.beats()) - 5;
			possibleEndTimes.add(endPositionBeforeNextSound);
			endCalculationData.maxEndTime = min(endPositionBeforeNextSound, endCalculationData.maxEndTime);
		}

		final HandShape handShape = lastBeforeEqual(chartData.currentHandShapes(), sound).find();
		if (handShape != null) {
			possibleEndTimes.add(handShape.endPosition().position(chartData.beats()));
		}

		endCalculationData.endPosition = possibleEndTimes.isEmpty() ? endCalculationData.position + 50
				: min(possibleEndTimes);
	}

	private void calculateMinSoundLength(final SoundEndCalculationData endCalculationData) {
		final double minSoundPosition = endCalculationData.position + minSoundLength;
		if (endCalculationData.endPosition < minSoundPosition) {
			endCalculationData.endPosition = min(endCalculationData.maxEndTime, minSoundPosition);
		}
	}

	private void calculateSoundLengthForNotes(final int noteId, final ChordOrNote sound,
			final SoundEndCalculationData endCalculationData) {
		if (sound == null) {
			return;
		}

		sound.notes()//
				.forEach(note -> {
					final ChordOrNote nextNote = findNextSoundOnString(note.string(), noteId + 1,
							chartData.currentSounds());
					if (nextNote != null) {
						endCalculationData.endPosition = min(endCalculationData.endPosition,
								nextNote.position(chartData.beats()) - 5);
					}
				});
	}

	private MidiChartNotePlayerNoteData makeNoteData(final int noteId) {
		final List<ChordOrNote> sounds = chartData.currentSounds();
		final ChordOrNote sound = sounds.get(noteId);
		final SoundEndCalculationData endCalculationData = new SoundEndCalculationData();
		endCalculationData.position = sound.position(chartData.beats());
		endCalculationData.endPosition = sound.endPosition().position(chartData.beats());
		endCalculationData.maxEndTime = chartTimeHandler.maxTime();

		final ChordOrNote nextSound = noteId + 1 < sounds.size() ? sounds.get(noteId + 1) : null;
		calculateEndForChord(sound, endCalculationData, nextSound);
		calculateMinSoundLength(endCalculationData);
		calculateSoundLengthForNotes(noteId, nextSound, endCalculationData);

		return new MidiChartNotePlayerNoteData(noteId, sound, endCalculationData.position,
				endCalculationData.endPosition);
	}

	public void startPlaying(final int speed) {
		this.speed = speed;
		if (modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		final double time = getTime();

		playing = true;
		final List<ChordOrNote> sounds = chartData.currentSounds();

		final Integer currentNoteId = lastBeforeEqual(sounds, FractionalPosition.fromTime(chartData.beats(), time))
				.findId();
		if (currentNoteId == null) {
			if (!sounds.isEmpty()) {
				nextSoundToPlay = makeNoteData(0);
			}

			return;
		}

		nextSoundToPlay = makeNoteData(currentNoteId);
		if (nextSoundToPlay.endPosition >= time) {
			playNextSound();
		} else {
			final int nextSoundId = nextSoundToPlay.noteId + 1;
			if (sounds.size() > nextSoundId) {
				nextSoundToPlay = makeNoteData(nextSoundId);
			} else {
				nextSoundToPlay = null;
			}
		}
	}

	public void stopPlaying() {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		playing = false;
		nextSoundToPlay = null;
		soundsPlaying.clear();
		midiNotePlayer.stopSound();
	}

}
