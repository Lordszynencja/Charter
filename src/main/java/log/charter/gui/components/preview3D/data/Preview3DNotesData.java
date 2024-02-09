package log.charter.gui.components.preview3D.data;

import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.song.notes.ChordOrNote.isLinkedToPrevious;
import static log.charter.song.notes.IConstantPosition.findLastIdBeforeEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.managers.RepeatManager;
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
	private static void addChord(final List<ChordBoxDrawData> chords, final List<List<NoteDrawData>> notes,
			final ArrangementChart arrangement, final Level level, final ArrayList2<ChordOrNote> sounds, final int id,
			final Chord chord, final int timeFrom, final int timeTo) {
		final ChordNotesVisibility chordNotesVisibility = chord.chordNotesVisibility(level.shouldChordShowNotes(id));

		if (chordNotesVisibility == ChordNotesVisibility.NONE) {
			chords.add(new ChordBoxDrawData(chord.position(), chord.chordNotesValue(n -> n.mute, Mute.NONE), true));
			return;
		}

		if (!chord.splitIntoNotes) {
			chords.add(new ChordBoxDrawData(chord.position(), Mute.NONE, false));
		}

		final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.templateId());
		final boolean shouldHaveLength = chordNotesVisibility == ChordNotesVisibility.TAILS;
		for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
			final int string = chordNoteEntry.getKey();
			final ChordNote chordNote = chordNoteEntry.getValue();
			final boolean linkPrevious = isLinkedToPrevious(string, id, sounds);
			final int fret = chordTemplate.frets.get(string);

			notes.get(string).add(
					new NoteDrawData(timeFrom, timeTo, chord, string, fret, chordNote, linkPrevious, shouldHaveLength));
		}
	}

	public static Preview3DNotesData getNotesForTimeSpan(final ChartData data, final int timeFrom, final int timeTo) {
		if (data.getCurrentArrangementLevel() == null) {
			final List<List<NoteDrawData>> notes = new ArrayList<>();
			for (int i = 0; i < maxStrings; i++) {
				notes.add(new ArrayList<>());
			}
			final List<ChordBoxDrawData> chords = new ArrayList<>();

			return new Preview3DNotesData(notes, chords);
		}

		final List<ChordBoxDrawData> chords = new ArrayList<>();
		final List<List<NoteDrawData>> notes = new ArrayList<>();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final Level level = data.getCurrentArrangementLevel();
		for (int i = 0; i < arrangement.tuning.strings; i++) {
			notes.add(new ArrayList<>());
		}
		final ArrayList2<ChordOrNote> sounds = level.chordsAndNotes;

		final int soundsTo = findLastIdBeforeEqual(sounds, timeTo);

		for (int i = 0; i <= soundsTo; i++) {
			final ChordOrNote sound = sounds.get(i);
			if (sound.endPosition() < timeFrom) {
				continue;
			}

			if (sound.isNote()) {
				final Note note = sound.note;
				notes.get(note.string)
						.add(new NoteDrawData(timeFrom, timeTo, note, isLinkedToPrevious(note.string, i, sounds)));
			} else {
				addChord(chords, notes, arrangement, level, sounds, i, sound.chord, timeFrom, timeTo);
			}
		}

		return new Preview3DNotesData(notes, chords);
	}

	public static Preview3DNotesData getNotesForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final int timeFrom, final int timeTo) {
		int maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.getRepeatEnd() - 1);
		}

		final Preview3DNotesData notesToDraw = getNotesForTimeSpan(data, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return notesToDraw;
		}

		final Preview3DNotesData repeatedNotes = getNotesForTimeSpan(data, repeatManager.getRepeatStart(),
				repeatManager.getRepeatEnd() - 1);
		int repeatStart = repeatManager.getRepeatEnd();
		while (repeatStart < timeFrom) {
			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		while (repeatStart < timeTo) {
			for (int string = 0; string < repeatedNotes.notes.size(); string++) {
				final List<NoteDrawData> stringNotesToDraw = notesToDraw.notes.get(string);

				for (final NoteDrawData note : repeatedNotes.notes.get(string)) {
					final int truePosition = note.originalPosition - repeatManager.getRepeatStart() + repeatStart;
					final int start = note.position - repeatManager.getRepeatStart() + repeatStart;
					int end = start + note.endPosition - note.position;
					if (start > timeTo) {
						break;
					}
					if (end > timeTo) {
						end = timeTo;
					}

					stringNotesToDraw.add(new NoteDrawData(truePosition, start, end, note));
				}
			}

			for (final ChordBoxDrawData chord : repeatedNotes.chords) {
				final int start = chord.position - repeatManager.getRepeatStart() + repeatStart;
				if (start > timeTo) {
					break;
				}

				notesToDraw.chords.add(new ChordBoxDrawData(start, chord));
			}

			repeatStart += repeatManager.getRepeatEnd() - repeatManager.getRepeatStart();
		}

		return notesToDraw;
	}

	public final List<List<NoteDrawData>> notes;
	public final List<ChordBoxDrawData> chords;

	public Preview3DNotesData(final List<List<NoteDrawData>> notes, final List<ChordBoxDrawData> chords) {
		this.notes = notes;
		this.chords = chords;
	}
}
