package log.charter.gui.handlers.midiPlayer;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static log.charter.data.config.Config.midiDelay;
import static log.charter.song.notes.IConstantPosition.findLastBeforeEqual;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class MidiChartNotePlayer {
	private ChartData data;
	private ModeManager modeManager;
	private final MidiNotePlayer midiNotePlayer = new MidiNotePlayer();

	private boolean playing = false;
	private int speed = 100;
	private final List<MidiChartNotePlayerNoteData> sounds = new ArrayList<>();
	private MidiChartNotePlayerNoteData nextSound;

	public void init(final ChartData data, final ModeManager modeManager) {
		this.data = data;
		this.modeManager = modeManager;

		midiNotePlayer.init(data);
	}

	private int getCurrentTime() {
		return data.nextTime + midiDelay * speed / 100;
	}

	private void stopSound(final MidiChartNotePlayerNoteData sound) {
		if (sound.sound.isNote()) {
			if (!sound.sound.note.linkNext) {
				midiNotePlayer.stopSound(sound.sound.note.string);
			}
		} else {
			sound.sound.chord.chordNotes.forEach((string, chordNote) -> {
				if (!chordNote.linkNext) {
					midiNotePlayer.stopSound(string);
				}
			});
		}
	}

	private void playNextSound() {
		if (nextSound == null) {
			return;
		}

		midiNotePlayer.playSound(nextSound.sound);
		sounds.add(nextSound);
		final int newNextSoundId = nextSound.noteId + 1;
		final ArrayList2<ChordOrNote> chartSounds = data.getCurrentArrangementLevel().sounds;
		if (chartSounds.size() > newNextSoundId) {
			nextSound = makeNoteData(newNextSoundId);
		} else {
			nextSound = null;
		}
	}

	private double getBendValue(final List<BendValue> bendValues, final int position, final int length) {
		if (bendValues.isEmpty()) {
			return 0;
		}

		final int insidePosition = getCurrentTime() - position;
		final int bendPointId = findLastIdBeforeEqual(bendValues, insidePosition);
		BendValue bendPointA;
		BendValue bendPointB;
		if (bendPointId == -1) {
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

	private double getSlideValue(final int position, final int length, final int slideLength,
			final boolean unpitchedSlide) {
		final double progress = 1.0 * (getCurrentTime() - position) / length;

		final double weight = unpitchedSlide//
				? 1 - sin((1 - progress) * Math.PI / 2)//
				: pow(sin(progress * Math.PI / 2), 3);
		return slideLength * weight;
	}

	private void updateBend(final int position, final int length, final int string, final List<BendValue> bendValues,
			final int fret, final Integer slideTo, final boolean unpitchedSlide) {
		double bendValue = getBendValue(bendValues, position, length);

		if (slideTo != null) {
			bendValue += getSlideValue(position, length, slideTo - fret, unpitchedSlide);
		}

		midiNotePlayer.updateBend(string, fret, bendValue);
	}

	private void updateSoundingSounds() {
		for (final MidiChartNotePlayerNoteData sound : sounds) {
			if (sound.endPosition < getCurrentTime()) {
				stopSound(sound);
				continue;
			}

			if (sound.sound.isNote()) {
				final Note note = sound.sound.note;
				updateBend(note.position(), note.length(), note.string, note.bendValues, note.fret, note.slideTo,
						note.unpitchedSlide);
			} else {
				final Chord chord = sound.sound.chord;
				final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.templateId());
				for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
					final int string = chordNoteEntry.getKey();
					final ChordNote chordNote = chordNoteEntry.getValue();
					updateBend(chord.position(), chordNote.length, string, chordNote.bendValues,
							chordTemplate.frets.get(string), chordNote.slideTo, chordNote.unpitchedSlide);
				}
			}
		}

		sounds.removeIf(sound -> sound.endPosition < getCurrentTime());
	}

	public void frame() {
		if (!playing || modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		midiNotePlayer.updateVolume();
		updateSoundingSounds();

		if (nextSound != null && nextSound.sound.position() <= getCurrentTime()) {
			playNextSound();
		}
	}

	private MidiChartNotePlayerNoteData makeNoteData(final int noteId) {
		final ChordOrNote sound = data.getCurrentArrangementLevel().sounds.get(noteId);
		int soundEndTime = sound.endPosition();
		int maxEndTime = data.songChart.beatsMap.songLengthMs;
		if (sound.isChord() && sound.length() < 50) {
			Integer newEndTime = null;
			if (noteId + 1 < data.getCurrentArrangementLevel().sounds.size()) {
				newEndTime = data.getCurrentArrangementLevel().sounds.get(noteId + 1).position() - 5;
				maxEndTime = min(newEndTime, maxEndTime);
			}

			final HandShape handShape = findLastBeforeEqual(data.getCurrentArrangementLevel().handShapes,
					sound.position());
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
			final ChordOrNote nextNote = ChordOrNote.findNextSoundOnString(sound.note.string, noteId + 1,
					data.getCurrentArrangementLevel().sounds);
			if (nextNote != null) {
				soundEndTime = min(soundEndTime, nextNote.position() - 5);
			}
		} else {
			for (final int string : sound.chord.chordNotes.keySet()) {
				final ChordOrNote nextNote = ChordOrNote.findNextSoundOnString(string, noteId + 1,
						data.getCurrentArrangementLevel().sounds);
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

		playing = true;
		final ArrayList2<ChordOrNote> chartSounds = data.getCurrentArrangementLevel().sounds;

		final int currentNoteId = findLastIdBeforeEqual(chartSounds, getCurrentTime());
		if (currentNoteId != -1) {
			nextSound = makeNoteData(currentNoteId);
			if (nextSound.endPosition >= getCurrentTime()) {
				playNextSound();
			} else {
				final int nextSoundId = nextSound.noteId + 1;
				if (chartSounds.size() > nextSoundId) {
					nextSound = makeNoteData(nextSoundId);
				} else {
					nextSound = null;
				}
			}
		} else if (!chartSounds.isEmpty()) {
			nextSound = makeNoteData(0);
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
