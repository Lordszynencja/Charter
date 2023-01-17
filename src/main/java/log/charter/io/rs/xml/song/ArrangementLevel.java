package log.charter.io.rs.xml.song;

import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.io.Logger;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.song.ChordTemplate;
import log.charter.song.Level;
import log.charter.song.enums.Mute;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.SeekableList;

@XStreamAlias("level")
public class ArrangementLevel {
	public static ArrayList2<ArrangementLevel> fromLevels(final HashMap2<Integer, Level> levels,
			final ArrayList2<ChordTemplate> chordTemplates) {
		final ArrayList2<ArrangementLevel> arrangementLevels = levels
				.map((difficulty, level) -> new ArrangementLevel(difficulty, level, chordTemplates));

		arrangementLevels.sort((a, b) -> Integer.compare(a.difficulty, b.difficulty));

		return arrangementLevels;
	}

	@XStreamAsAttribute
	public int difficulty;

	public CountedList<ArrangementNote> notes;
	public CountedList<ArrangementChord> chords;
	public CountedList<ArrangementFretHandMute> fretHandMutes;
	public CountedList<ArrangementAnchor> anchors;
	public CountedList<ArrangementHandShape> handShapes;

	public ArrangementLevel() {
	}

	private ArrangementLevel(final int difficulty, final Level level, final ArrayList2<ChordTemplate> chordTemplates) {
		this.difficulty = difficulty;

		setChordsAndNotes(chordTemplates, level.chordsAndNotes);

		fretHandMutes = new CountedList<>();
		anchors = new CountedList<>(level.anchors.map(ArrangementAnchor::new));
		handShapes = new CountedList<ArrangementHandShape>(level.handShapes.map(ArrangementHandShape::new));

		addChordNotesForFirstChordsInHandShape(level.chordsAndNotes, chordTemplates);
		addChordNotesForChordsWithDifferentShapeThanHandShape(chordTemplates);
	}

	private void setChordsAndNotes(final ArrayList2<ChordTemplate> chordTemplates,
			final ArrayList2<ChordOrNote> chordsAndNotes) {
		notes = new CountedList<>();
		chords = new CountedList<>();

		for (int i = 0; i < chordsAndNotes.size(); i++) {
			final ChordOrNote sound = chordsAndNotes.get(i);
			if (sound.isNote()) {
				notes.list.add(new ArrangementNote(sound.note));
				continue;
			}

			final Chord chord = sound.chord;
			final ChordTemplate chordTemplate = chordTemplates.get(chord.chordId);
			final int nextPosition = i + 1 < chordsAndNotes.size() ? chordsAndNotes.get(i + 1).position()
					: chord.position() + 100;
			final ArrangementChord arrangementChord = new ArrangementChord(chord, chordTemplate, nextPosition);

			if (i > 0 && chordsAndNotes.get(i - 1).asGuitarSound().linkNext) {
				arrangementChord.populateChordNotes(chordTemplate);
				notes.list.addAll(arrangementChord.chordNotes);
				continue;
			}

			chords.list.add(arrangementChord);
		}
	}

	private void addChordNotesForFirstChordsInHandShape(final ArrayList2<ChordOrNote> chordsAndNotes,
			final ArrayList2<ChordTemplate> chordTemplates) {
		final SeekableList<ChordOrNote> seekableNotes = new SeekableList<>(chordsAndNotes);
		final SeekableList<ArrangementChord> seekableArrangementChords = new SeekableList<>(chords.list);

		for (final ArrangementHandShape handShape : handShapes.list) {
			seekableNotes.seekNextGreaterEqual(handShape.startTime);

			if (!seekableNotes.hasPosition()) {
				return;
			}

			final ChordOrNote chordOrNote = seekableNotes.getCurrent();
			if (!chordOrNote.isChord() || chordOrNote.position() > handShape.endTime
					|| chordOrNote.chord.mute != Mute.NONE) {
				continue;
			}

			seekableArrangementChords.seekNextGreaterEqual(chordOrNote.chord.position());
			final ArrangementChord chord = seekableArrangementChords.getCurrent();
			final ChordTemplate chordTemplate = chordTemplates.get(chord.chordId);
			if (chord.chordNotes != null && !chord.chordNotes.isEmpty()) {
				continue;
			}

			chord.populateChordNotes(chordTemplate);
		}
	}

	private void addChordNotesForChordsWithDifferentShapeThanHandShape(final ArrayList2<ChordTemplate> chordTemplates) {
		for (final ArrangementChord chord : chords.list) {
			final int handShapeId = findLastIdBeforeEqual(handShapes.list, chord.time);
			if (handShapeId == -1) {
				Logger.error("Chord has no hand shape after adding them!");
				continue;
			}

			final ArrangementHandShape handShape = handShapes.list.get(handShapeId);
			if (handShape.chordId != chord.chordId) {
				chord.populateChordNotes(chordTemplates.get(chord.chordId));
			}
		}
	}
}
