package log.charter.gui.handlers.midiPlayer;

import static java.lang.Math.min;
import static log.charter.data.config.Config.midiDelay;
import static log.charter.song.notes.IPosition.findLastBeforeEqual;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.song.BendValue;
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
			midiNotePlayer.stopSound(sound.sound.note.string);
		} else {
			for (final Integer string : sound.sound.chord.chordNotes.keySet()) {
				midiNotePlayer.stopSound(string);
			}
		}
	}

	private void playNextSound() {
		midiNotePlayer.playSound(nextSound.sound);
		sounds.add(nextSound);
		final int nextSoundId = nextSound.noteId + 1;
		final ArrayList2<ChordOrNote> chartSounds = data.getCurrentArrangementLevel().chordsAndNotes;
		if (chartSounds.size() > nextSoundId) {
			nextSound = makeNoteData(nextSoundId);
		} else {
			nextSound = null;
		}
	}

	private void updateBend(final int position, final int length, final int string, final List<BendValue> bendValues) {
		if (bendValues.isEmpty()) {
			return;
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
		final double bendValue = weight * (bendValueB - bendValueA) + bendValueA;
		midiNotePlayer.updateBend(string, bendValue);
	}

	private void updateSoundingSounds() {
		for (final MidiChartNotePlayerNoteData sound : sounds) {
			if (sound.endPosition < getCurrentTime()) {
				stopSound(sound);
				continue;
			}

			if (sound.sound.isNote()) {
				final Note note = sound.sound.note;
				updateBend(note.position(), note.length(), note.string, note.bendValues);
			} else {
				final Chord chord = sound.sound.chord;
				for (final Entry<Integer, ChordNote> chordNote : chord.chordNotes.entrySet()) {
					updateBend(chord.position(), chordNote.getValue().length, chordNote.getKey(),
							chordNote.getValue().bendValues);
				}
			}
		}

		sounds.removeIf(sound -> sound.endPosition < getCurrentTime());
	}

	public void frame() {
		if (!playing || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		updateSoundingSounds();

		if (nextSound != null && nextSound.sound.position() <= getCurrentTime()) {
			playNextSound();
		}
	}

	private MidiChartNotePlayerNoteData makeNoteData(final int noteId) {
		final ChordOrNote sound = data.getCurrentArrangementLevel().chordsAndNotes.get(noteId);
		int soundEndTime = sound.endPosition();
		int maxEndTime = data.songChart.beatsMap.songLengthMs;
		if (sound.isChord() && sound.length() == 0) {
			Integer newEndTime = null;
			if (noteId + 1 < data.getCurrentArrangementLevel().chordsAndNotes.size()) {
				newEndTime = data.getCurrentArrangementLevel().chordsAndNotes.get(noteId + 1).position() - 5;
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

		return new MidiChartNotePlayerNoteData(noteId, sound, soundEndTime);
	}

	public void startPlaying(final int speed) {
		this.speed = speed;
		if (modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		playing = true;
		final ArrayList2<ChordOrNote> chartSounds = data.getCurrentArrangementLevel().chordsAndNotes;

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
		if (modeManager.editMode != EditMode.GUITAR) {
			return;
		}
		playing = false;

		nextSound = null;
		sounds.clear();
		midiNotePlayer.stopSound();
	}

}
