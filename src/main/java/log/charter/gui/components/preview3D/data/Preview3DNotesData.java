package log.charter.gui.components.preview3D.data;

import static log.charter.song.notes.ChordOrNote.findPreviousSoundOnString;
import static log.charter.song.notes.IPosition.findLastIdBefore;
import static log.charter.song.notes.IPositionWithLength.findFirstIdAfterEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.config.Config;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.Level;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Chord.ChordNotesVisibility;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DNotesData {
	public final List<List<Preview3DNoteData>> notes;
	public final List<Preview3DChordDrawingData> chords;

	public Preview3DNotesData(final ArrangementChart arrangement, final Level level, final int fromTime,
			final int toTime) {
		chords = new ArrayList<>();

		notes = new ArrayList<>();
		for (int i = 0; i < Config.maxStrings; i++) {
			notes.add(new ArrayList<>());
		}
		final ArrayList2<ChordOrNote> sounds = level.chordsAndNotes;

		int soundsFrom = findFirstIdAfterEqual(sounds, fromTime);
		if (soundsFrom == -1) {
			soundsFrom = 0;
		}
		final int soundsTo = findLastIdBefore(sounds, toTime);

		for (int i = soundsFrom; i <= soundsTo; i++) {
			final ChordOrNote sound = sounds.get(i);
			if (sound.isNote()) {
				addNote(sounds, i, sound.note);
			} else {
				addChord(arrangement, level, sounds, i, sound.chord);
			}
		}
	}

	private boolean isLinkedToPrevious(final int string, final int id, final ArrayList2<ChordOrNote> sounds) {
		final ChordOrNote previousSound = findPreviousSoundOnString(string, id - 1, sounds);
		return previousSound != null && previousSound.linkNext(string);
	}

	private void addNote(final ArrayList2<ChordOrNote> sounds, final int id, final Note note) {
		notes.get(note.string).add(new Preview3DNoteData(note, isLinkedToPrevious(note.string, id, sounds)));
	}

	private void addChord(final ArrangementChart arrangement, final Level level, final ArrayList2<ChordOrNote> sounds,
			final int id, final Chord chord) {
		final ChordNotesVisibility chordNotesVisibility = chord.chordNotesVisibility(level.shouldChordShowNotes(id));

		if (chordNotesVisibility == ChordNotesVisibility.NONE) {
			chords.add(new Preview3DChordDrawingData(chord.position(), chord.chordNotesValue(n -> n.mute, Mute.NONE)));
		} else {
			if (!chord.splitIntoNotes) {
				chords.add(new Preview3DChordDrawingData(chord.position(), Mute.NONE));
			}

			final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.templateId());
			final boolean shouldHaveLength = chordNotesVisibility == ChordNotesVisibility.TAILS;
			for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
				final int string = chordNoteEntry.getKey();
				final ChordNote chordNote = chordNoteEntry.getValue();
				final boolean linkPrevious = isLinkedToPrevious(string, id, sounds);
				notes.get(string).add(new Preview3DNoteData(chord, string, chordTemplate.frets.get(string), chordNote,
						linkPrevious, shouldHaveLength));
			}
		}
	}

}
