package log.charter.song;

import static log.charter.song.notes.IConstantPosition.findLastBeforeEqual;
import static log.charter.util.Utils.mapInteger;

import java.util.List;
import java.util.Map.Entry;

import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementLevel;
import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.Pair;

public class Level {
	public static HashMap2<Integer, Level> fromArrangementLevels(final ArrangementChart arrangement,
			final List<ArrangementLevel> arrangementLevels) {
		final HashMap2<Integer, Level> levels = new ArrayList2<>(arrangementLevels)//
				.toMap(arrangementLevel -> new Pair<>(arrangementLevel.difficulty,
						new Level(arrangementLevel, arrangement)));

		if (levels.get(0) == null) {
			levels.put(0, new Level());
		}

		return levels;
	}

	public ArrayList2<Anchor> anchors = new ArrayList2<>();
	public ArrayList2<ChordOrNote> chordsAndNotes = new ArrayList2<>();
	public ArrayList2<HandShape> handShapes = new ArrayList2<>();

	public Level() {
	}

	private Level(final ArrangementLevel arrangementLevel, final ArrangementChart arrangement) {
		anchors = arrangementLevel.anchors.list.map(Anchor::new);
		handShapes = arrangementLevel.handShapes.list.map(HandShape::new);

		for (final ArrangementChord arrangementChord : arrangementLevel.chords.list) {
			chordsAndNotes.add(new ChordOrNote(
					new Chord(arrangementChord, arrangement.chordTemplates.get(arrangementChord.chordId))));
		}

		final HashMap2<Integer, ArrayList2<ArrangementNote>> arrangementNotesMap = new HashMap2<>();
		for (final ArrangementNote arrangementNote : arrangementLevel.notes.list) {
			ArrayList2<ArrangementNote> positionNotes = arrangementNotesMap.get(arrangementNote.time);
			if (positionNotes == null) {
				positionNotes = new ArrayList2<>();
				arrangementNotesMap.put(arrangementNote.time, positionNotes);
			}

			positionNotes.add(arrangementNote);
		}

		for (final Entry<Integer, ArrayList2<ArrangementNote>> notesPosition : arrangementNotesMap.entrySet()) {
			if (notesPosition.getValue().size() == 1) {
				chordsAndNotes.add(new ChordOrNote(new Note(notesPosition.getValue().get(0))));
				continue;
			}

			final ChordTemplate specialTemplate = new ChordTemplate();
			for (final ArrangementNote arrangementNote : notesPosition.getValue()) {
				specialTemplate.frets.put(arrangementNote.string, arrangementNote.fret);
			}
			final int chordId = arrangement.getChordTemplateIdWithSave(specialTemplate);

			final Chord chord = new Chord(notesPosition.getKey(), chordId, arrangement.chordTemplates.get(chordId));
			chord.splitIntoNotes = true;
			for (final ArrangementNote arrangementNote : notesPosition.getValue()) {
				final ChordNote chordNote = chord.chordNotes.get(arrangementNote.string);
				chordNote.linkNext = mapInteger(arrangementNote.linkNext);
				chordNote.length = arrangementNote.sustain == null ? 0 : arrangementNote.sustain;

				if (arrangementNote.bendValues != null && !arrangementNote.bendValues.list.isEmpty()) {
					for (final ArrangementBendValue bendValue : arrangementNote.bendValues.list) {
						chordNote.bendValues.add(new BendValue(bendValue, arrangementNote.time));
					}
				}

				if (arrangementNote.slideTo != null) {
					chordNote.slideTo = arrangementNote.slideTo;
				}
				if (arrangementNote.slideUnpitchTo != null) {
					chordNote.slideTo = arrangementNote.slideUnpitchTo;
					chordNote.unpitchedSlide = true;
				}
			}

			chordsAndNotes.add(new ChordOrNote(chord));
		}

		chordsAndNotes.sort(null);

		for (int i = 0; i < chordsAndNotes.size() - 1; i++) {
			final ChordOrNote sound = chordsAndNotes.get(i);
			final ChordOrNote nextSound = chordsAndNotes.get(i + 1);
			if (sound.endPosition() >= nextSound.position()) {
				sound.asGuitarSound().passOtherNotes = true;
			}
		}
	}

	public boolean shouldChordShowNotes(final int id) {
		final Chord chord = chordsAndNotes.get(id).chord;
		final HandShape handShape = findLastBeforeEqual(handShapes, chord.position());
		if (handShape == null) {
			return true;
		}
		if (handShape.templateId != chord.templateId()) {
			return true;
		}

		for (int j = id - 1; j >= 0; j--) {
			final ChordOrNote previousSound = chordsAndNotes.get(j);
			if (previousSound.isNote()//
					|| previousSound.chord.fullyMuted()) {
				continue;
			}
			if (previousSound.chord.templateId() != handShape.templateId) {
				return true;
			}
			if (previousSound.position() < handShape.position()) {
				break;
			}

			return false;
		}

		return true;
	}
}
