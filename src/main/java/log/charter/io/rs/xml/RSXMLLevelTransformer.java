package log.charter.io.rs.xml;

import static log.charter.util.CollectionUtils.map;
import static log.charter.util.CollectionUtils.toMap;
import static log.charter.util.Utils.mapInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.io.rs.xml.song.ArrangementAnchor;
import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementHandShape;
import log.charter.io.rs.xml.song.ArrangementLevel;
import log.charter.io.rs.xml.song.ArrangementNote;

public class RSXMLLevelTransformer {
	public static List<Level> fromArrangementDataLevels(final Arrangement arrangement,
			final List<ArrangementLevel> arrangementLevels, final ImmutableBeatsMap beats) {
		final Map<Integer, Level> levelsMap = toMap(arrangementLevels, (map, arrangementLevel) -> map
				.put(arrangementLevel.difficulty, toLevel(arrangementLevel, arrangement, beats)));

		final List<Level> levels = new ArrayList<>();
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

	private static Anchor anchor(final ImmutableBeatsMap beats, final ArrangementAnchor arrangementAnchor) {
		final FractionalPosition position = FractionalPosition.fromTime(beats, arrangementAnchor.time, true);
		final int fret = arrangementAnchor.fret;
		final int width = arrangementAnchor.width == null ? 4 : arrangementAnchor.width.intValue();

		return new Anchor(position, fret, width);
	}

	private static HandShape handShape(final ImmutableBeatsMap beats, final ArrangementHandShape arrangementHandShape) {
		final FractionalPosition position = FractionalPosition.fromTime(beats, arrangementHandShape.startTime, true);
		final FractionalPosition endPosition = FractionalPosition.fromTime(beats, arrangementHandShape.endTime, true);

		return new HandShape(position, endPosition, arrangementHandShape.chordId);
	}

	private static Level toLevel(final ArrangementLevel arrangementLevel, final Arrangement arrangement,
			final ImmutableBeatsMap beats) {
		final Level level = new Level();
		level.anchors = map(arrangementLevel.anchors.list, a -> anchor(beats, a));
		level.handShapes = map(arrangementLevel.handShapes.list, h -> handShape(beats, h));

		for (final ArrangementChord arrangementChord : arrangementLevel.chords.list) {
			level.sounds.add(ChordOrNote
					.from(new Chord(arrangementChord, arrangement.chordTemplates.get(arrangementChord.chordId))));
		}

		final Map<Integer, List<ArrangementNote>> arrangementNotesMap = new HashMap<>();
		for (final ArrangementNote arrangementNote : arrangementLevel.notes.list) {
			List<ArrangementNote> positionNotes = arrangementNotesMap.get(arrangementNote.time);
			if (positionNotes == null) {
				positionNotes = new ArrayList<>();
				arrangementNotesMap.put(arrangementNote.time, positionNotes);
			}

			positionNotes.add(arrangementNote);
		}

		for (final Entry<Integer, List<ArrangementNote>> notesPosition : arrangementNotesMap.entrySet()) {
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

		level.sounds.sort(IConstantPosition::compareTo);

		for (int i = 0; i < level.sounds.size() - 1; i++) {
			final ChordOrNote sound = level.sounds.get(i);
			final ChordOrNote nextSound = level.sounds.get(i + 1);
			if (sound.endPosition().position() >= nextSound.position()) {
				sound.asGuitarSound().passOtherNotes = true;
			}
		}

		return level;
	}

}
