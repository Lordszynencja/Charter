package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.Utils.mapInteger;

import java.util.List;
import java.util.Map.Entry;

import log.charter.data.config.Config;
import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementLevel;
import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.Pair;

public class Level {
	public static HashMap2<Integer, Level> fromArrangementLevels(final ArrangementChart arrangement,
			final List<ArrangementLevel> arrangementLevels) {
		return new ArrayList2<>(arrangementLevels).toMap(
				arrangementLevel -> new Pair<>(arrangementLevel.difficulty, new Level(arrangementLevel, arrangement)));
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
			chordsAndNotes.add(new ChordOrNote(new Chord(arrangementChord)));
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

			final Chord chord = new Chord(notesPosition.getKey(), chordId);
			for (final ArrangementNote arrangementNote : notesPosition.getValue()) {
				chord.linkNext |= mapInteger(arrangementNote.linkNext);
				chord.length(max(chord.length(), arrangementNote.sustain == null ? 0 : arrangementNote.sustain));

				if (arrangementNote.bendValues != null && !arrangementNote.bendValues.list.isEmpty()) {
					final ArrayList2<BendValue> noteBendValues = new ArrayList2<>();
					chord.bendValues.put(arrangementNote.string, noteBendValues);
					for (final ArrangementBendValue bendValue : arrangementNote.bendValues.list) {
						noteBendValues.add(new BendValue(bendValue));
					}
				}

				if (arrangementNote.slideTo != null) {
					chord.slideTo = min(arrangementNote.slideTo, chord.slideTo == null ? Config.frets : chord.slideTo);
				}
				if (arrangementNote.slideUnpitchTo != null) {
					chord.slideTo = min(arrangementNote.slideUnpitchTo,
							chord.slideTo == null ? Config.frets : chord.slideTo);
					chord.unpitchedSlide = true;
				}
			}
			chordsAndNotes.add(new ChordOrNote(chord));
		}

		chordsAndNotes.sort(null);
	}
}
