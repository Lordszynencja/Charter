package log.charter.gui.components.preview3D.data;

import static java.lang.Math.min;
import static log.charter.data.config.Config.maxStrings;
import static log.charter.data.song.notes.ChordOrNote.isLinkedToPrevious;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.Level;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.Chord.ChordNotesVisibility;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.services.RepeatManager;

public class Preview3DNotesData {
	private static void addChord(final ImmutableBeatsMap beats, final List<ChordBoxDrawData> chords,
			final List<List<NoteDrawData>> notes, final Arrangement arrangement, final Level level,
			final List<ChordOrNote> sounds, final int id, final Chord chord, final double timeFrom,
			final double timeTo) {
		final ChordNotesVisibility chordNotesVisibility = chord
				.chordNotesVisibility(level.shouldChordShowNotes(beats, id));

		if (chordNotesVisibility == ChordNotesVisibility.NONE) {
			chords.add(new ChordBoxDrawData(chord.position(beats), //
					chord.chordNotesValue(n -> n.mute, Mute.NONE), //
					true, //
					chord.chordNotes.size() > 2));
			return;
		}

		if (!chord.splitIntoNotes) {
			chords.add(new ChordBoxDrawData(chord.position(beats), //
					Mute.NONE, //
					false, //
					chord.chordNotes.size() > 2));
		}
		if (chord.forceNoNotes) {
			return;
		}

		final double notePosition = chord.position(beats);
		final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.templateId());
		final boolean shouldHaveLength = chordNotesVisibility == ChordNotesVisibility.TAILS;
		for (final Entry<Integer, ChordNote> chordNoteEntry : chord.chordNotes.entrySet()) {
			final int string = chordNoteEntry.getKey();
			final ChordNote chordNote = chordNoteEntry.getValue();
			final double noteEndPosition = chordNote.endPosition(beats);
			final boolean linkPrevious = isLinkedToPrevious(string, id, sounds);
			final int fret = chordTemplate.frets.get(string);

			notes.get(string).add(new NoteDrawData(notePosition, noteEndPosition, timeFrom, timeTo, chord, string, fret,
					chordNote, linkPrevious, shouldHaveLength));
		}
	}

	public static Preview3DNotesData getNotesForTimeSpan(final ChartData chartData, final double timeFrom,
			final double timeTo) {
		if (chartData.currentArrangementLevel() == null) {
			final List<List<NoteDrawData>> notes = new ArrayList<>();
			for (int i = 0; i < maxStrings; i++) {
				notes.add(new ArrayList<>());
			}
			final List<ChordBoxDrawData> chords = new ArrayList<>();

			return new Preview3DNotesData(notes, chords);
		}

		final ImmutableBeatsMap beats = chartData.beats();
		final List<ChordBoxDrawData> chords = new ArrayList<>();
		final List<List<NoteDrawData>> notes = new ArrayList<>();
		final Arrangement arrangement = chartData.currentArrangement();
		final Level level = chartData.currentArrangementLevel();
		for (int i = 0; i < maxStrings; i++) {
			notes.add(new ArrayList<>());
		}
		final List<ChordOrNote> sounds = level.sounds;

		final Integer soundsTo = lastBeforeEqual(sounds, FractionalPosition.fromTime(beats, timeTo)).findId();
		if (soundsTo == null) {
			return new Preview3DNotesData(notes, chords);
		}

		for (int i = 0; i <= soundsTo; i++) {
			final ChordOrNote sound = sounds.get(i);
			if (sound.endPosition().position(beats) < timeFrom) {
				continue;
			}

			if (sound.isNote()) {
				final Note note = sound.note();
				final double notePosition = note.position(beats);
				final double noteEndPosition = note.endPosition(beats);
				notes.get(note.string).add(new NoteDrawData(notePosition, noteEndPosition, timeFrom, timeTo, note,
						isLinkedToPrevious(note.string, i, sounds)));
			} else {
				addChord(chartData.beats(), chords, notes, arrangement, level, sounds, i, sound.chord(), timeFrom,
						timeTo);
			}
		}

		return new Preview3DNotesData(notes, chords);
	}

	public static Preview3DNotesData getNotesForTimeSpanWithRepeats(final ChartData data,
			final RepeatManager repeatManager, final double timeFrom, final double timeTo) {
		double maxTime = timeTo;
		if (repeatManager.isRepeating()) {
			maxTime = min(maxTime, repeatManager.repeatEnd() - 1);
		}

		final Preview3DNotesData notesToDraw = getNotesForTimeSpan(data, timeFrom, maxTime);

		if (!repeatManager.isRepeating()) {
			return notesToDraw;
		}

		final Preview3DNotesData repeatedNotes = getNotesForTimeSpan(data, repeatManager.repeatStart(),
				repeatManager.repeatEnd() - 1);
		double repeatStart = repeatManager.repeatEnd();
		final double repeatLength = repeatManager.repeatEnd() - repeatManager.repeatStart();
		while (repeatStart < timeFrom) {
			repeatStart += repeatLength;
		}

		while (repeatStart < timeTo) {
			final double repeatOffset = repeatStart - repeatManager.repeatStart();
			for (int string = 0; string < repeatedNotes.notes.size(); string++) {
				final List<NoteDrawData> stringNotesToDraw = notesToDraw.notes.get(string);

				for (final NoteDrawData note : repeatedNotes.notes.get(string)) {
					final double truePosition = note.originalPosition + repeatOffset;
					final double start = note.position + repeatOffset;
					double end = start + note.endPosition - note.position;
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
				final double start = chord.position - repeatManager.repeatStart() + repeatStart;
				if (start > timeTo) {
					break;
				}

				notesToDraw.chords.add(new ChordBoxDrawData(start, chord));
			}

			repeatStart += repeatLength;
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
