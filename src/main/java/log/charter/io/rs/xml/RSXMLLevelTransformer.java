package log.charter.io.rs.xml;

import static log.charter.util.Utils.mapInteger;

import java.util.List;
import java.util.Map.Entry;

import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementLevel;
import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.Pair;

public class RSXMLLevelTransformer {
	public static ArrayList2<Level> fromArrangementDataLevels(final Arrangement arrangement,
			final List<ArrangementLevel> arrangementLevels) {
		final HashMap2<Integer, Level> levelsMap = new ArrayList2<>(arrangementLevels)//
				.toMap(arrangementLevel -> new Pair<>(arrangementLevel.difficulty,
						toLevel(arrangementLevel, arrangement)));

		final ArrayList2<Level> levels = new ArrayList2<>();
		levelsMap.forEach((id, level) -> {
			while (levels.size() <= id) {
				levels.add(new Level());
			}

			levels.set(id, level);
		});

		if (levels.isEmpty()) {
			levels.add(new Level());
		}

		return levels;
	}

	private static Level toLevel(final ArrangementLevel arrangementLevel, final Arrangement arrangement) {
		final Level level = new Level();
		level.anchors = arrangementLevel.anchors.list.map(Anchor::new);
		level.handShapes = arrangementLevel.handShapes.list.map(HandShape::new);

		for (final ArrangementChord arrangementChord : arrangementLevel.chords.list) {
			level.sounds.add(ChordOrNote
					.from(new Chord(arrangementChord, arrangement.chordTemplates.get(arrangementChord.chordId))));
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
				level.sounds.add(ChordOrNote.from(new Note(notesPosition.getValue().get(0))));
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

			level.sounds.add(ChordOrNote.from(chord));
		}

		level.sounds.sort(null);

		for (int i = 0; i < level.sounds.size() - 1; i++) {
			final ChordOrNote sound = level.sounds.get(i);
			final ChordOrNote nextSound = level.sounds.get(i + 1);
			if (sound.endPosition() >= nextSound.position()) {
				sound.asGuitarSound().passOtherNotes = true;
			}
		}

		return level;
	}

}
