package log.charter.services.audio;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.data.config.Config.midiDelay;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.Position;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

public class MidiChartNotePlayer implements Initiable {
	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;
	private ModeManager modeManager;

	private final MidiNotePlayer midiNotePlayer = new MidiNotePlayer();

	private boolean playing = false;
	private int speed = 100;
	private final List<MidiChartNotePlayerNoteData> sounds = new ArrayList<>();
	private MidiChartNotePlayerNoteData nextSound;

	@Override
	public void init() {
		midiNotePlayer.init(chartData);
	}

	private int getTime() {
		return chartTimeHandler.time() + midiDelay * speed / 100;
	}

	private void stopSound(final MidiChartNotePlayerNoteData sound) {
		sound.sound.notes().forEach(note -> {
			if (!note.linkNext()) {
				midiNotePlayer.stopSound(note.string());
			}
		});
	}

	private void playNextSound() {
		if (nextSound == null) {
			return;
		}

		midiNotePlayer.playSound(nextSound.sound);
		sounds.add(nextSound);
		final int newNextSoundId = nextSound.noteId + 1;
		final List<ChordOrNote> chartSounds = chartData.currentSounds();
		if (chartSounds.size() > newNextSoundId) {
			nextSound = makeNoteData(newNextSoundId);
		} else {
			nextSound = null;
		}
	}

	private double getBendValue(final int time, final List<BendValue> bendValues, final int position,
			final int length) {
		if (bendValues.isEmpty()) {
			return 0;
		}

		final int insidePosition = time - position;
		final Integer bendPointId = lastBeforeEqual(bendValues, new Position(insidePosition)).findId();
		BendValue bendPointA;
		BendValue bendPointB;
		if (bendPointId == null) {
			bendPointA = new BendValue(0, BigDecimal.ZERO);
			bendPointB = bendValues.get(0);
		} else {
			bendPointA = bendValues.get(bendPointId);
			bendPointB = bendPointId + 1 < bendValues.size() ? bendValues.get(bendPointId + 1)
					: new BendValue(length, bendPointA.bendValue);
		}

		final double bendValueA = bendPointA.bendValue.doubleValue();
		final double bendValueB = bendPointB.bendValue.doubleValue();

		final double weight = 1.0 * (insidePosition - bendPointA.position())
				/ (bendPointB.position() - bendPointA.position());
		return weight * (bendValueB - bendValueA) + bendValueA;
	}

	private double getSlideValue(final int time, final int position, final int length, final int slideLength,
			final boolean unpitchedSlide) {
		final double progress = 1.0 * (time - position) / length;

		final double weight = unpitchedSlide//
				? 1 - sin((1 - progress) * Math.PI / 2)//
				: pow(sin(progress * Math.PI / 2), 3);
		return slideLength * weight;
	}

	private void updateBend(final int time, final int position, final int length, final int string,
			final List<BendValue> bendValues, final int fret, final Integer slideTo, final boolean unpitchedSlide) {
		double bendValue = getBendValue(time, bendValues, position, length);

		if (slideTo != null) {
			bendValue += getSlideValue(time, position, length, slideTo - fret, unpitchedSlide);
		}

		midiNotePlayer.updateBend(string, fret, bendValue);
	}

	private void updateSoundingSounds(final int time) {
		for (final MidiChartNotePlayerNoteData sound : sounds) {
			if (sound.endPosition < time) {
				stopSound(sound);
				continue;
			}

			sound.sound.notesWithFrets(chartData.currentArrangement().chordTemplates)//
					.forEach(note -> {
						updateBend(time, note.position(), note.length(), note.string(), note.bendValues(), note.fret(),
								note.slideTo(), note.unpitchedSlide());
					});

			if (sound.sound.isNote()) {
				final Note note = sound.sound.note();
				updateBend(time, note.position(), note.length(), note.string, note.bendValues, note.fret, note.slideTo,
						note.unpitchedSlide);
			} else {
				final Chord chord = sound.sound.chord();
				final ChordTemplate chordTemplate = chartData.currentArrangement().chordTemplates
						.get(chord.templateId());
				for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
					final int string = chordNoteEntry.getKey();
					final ChordNote chordNote = chordNoteEntry.getValue();
					updateBend(time, chord.position(), chordNote.length, string, chordNote.bendValues,
							chordTemplate.frets.get(string), chordNote.slideTo, chordNote.unpitchedSlide);
				}
			}
		}

		sounds.removeIf(sound -> sound.endPosition < time);
	}

	public void frame() {
		if (!playing || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		final int time = getTime();

		midiNotePlayer.updateVolume();
		updateSoundingSounds(time);

		if (nextSound != null && nextSound.sound.position() <= time) {
			playNextSound();
		}
	}

	private MidiChartNotePlayerNoteData makeNoteData(final int noteId) {
		final ChordOrNote sound = chartData.currentArrangementLevel().sounds.get(noteId);
		int soundEndTime = sound.endPosition();
		int maxEndTime = chartTimeHandler.maxTime();
		if (sound.isChord() && sound.length() < 50) {
			Integer newEndTime = null;
			if (noteId + 1 < chartData.currentArrangementLevel().sounds.size()) {
				newEndTime = chartData.currentArrangementLevel().sounds.get(noteId + 1).position() - 5;
				maxEndTime = min(newEndTime, maxEndTime);
			}

			final HandShape handShape = lastBeforeEqual(chartData.currentArrangementLevel().handShapes, sound).find();
			if (handShape != null && (newEndTime == null || handShape.endPosition() < newEndTime)) {
				newEndTime = handShape.endPosition();
			}

			if (newEndTime != null) {
				soundEndTime = newEndTime;
			}
		}

		if (soundEndTime < sound.position() + 100) {
			soundEndTime = min(maxEndTime, sound.position() + 100);
		}
		if (sound.isNote()) {
			final ChordOrNote nextNote = ChordOrNote.findNextSoundOnString(sound.note().string, noteId + 1,
					chartData.currentArrangementLevel().sounds);
			if (nextNote != null) {
				soundEndTime = min(soundEndTime, nextNote.position() - 5);
			}
		} else {
			for (final int string : sound.chord().chordNotes.keySet()) {
				final ChordOrNote nextNote = ChordOrNote.findNextSoundOnString(string, noteId + 1,
						chartData.currentArrangementLevel().sounds);
				if (nextNote != null) {
					soundEndTime = min(soundEndTime, nextNote.position() - 5);
				}
			}
		}

		return new MidiChartNotePlayerNoteData(noteId, sound, soundEndTime);
	}

	public void startPlaying(final int speed) {
		this.speed = speed;
		if (modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		final int time = getTime();

		playing = true;
		final List<ChordOrNote> chartSounds = chartData.currentArrangementLevel().sounds;

		final Integer currentNoteId = lastBeforeEqual(chartSounds, new Position(time)).findId();
		if (currentNoteId == null) {
			if (!chartSounds.isEmpty()) {
				nextSound = makeNoteData(0);
			}

			return;
		}

		nextSound = makeNoteData(currentNoteId);
		if (nextSound.endPosition >= time) {
			playNextSound();
		} else {
			final int nextSoundId = nextSound.noteId + 1;
			if (chartSounds.size() > nextSoundId) {
				nextSound = makeNoteData(nextSoundId);
			} else {
				nextSound = null;
			}
		}
	}

	public void stopPlaying() {
		if (modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		playing = false;
		nextSound = null;
		sounds.clear();
		midiNotePlayer.stopSound();
	}

}
