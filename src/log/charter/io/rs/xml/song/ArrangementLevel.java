package log.charter.io.rs.xml.song;

import java.util.LinkedList;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.Level;
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

	private ArrangementLevel(final int difficulty, final Level levelChart,
			final ArrayList2<ChordTemplate> chordTemplates) {
		this.difficulty = difficulty;

		setNotes(levelChart.chordsAndNotes);
		setChords(levelChart.chordsAndNotes, chordTemplates);

		fretHandMutes = new CountedList<>();
		anchors = new CountedList<>(levelChart.anchors.map(ArrangementAnchor::new));

		setHandShapes(levelChart.chordsAndNotes, levelChart.handShapes);

		addChordNotesForFirstChordsInHandShape(levelChart.chordsAndNotes, chordTemplates);
	}

	private void setNotes(final ArrayList2<ChordOrNote> chordsAndNotes) {
		notes = new CountedList<>();
		chordsAndNotes.stream()//
				.filter(chordOrNote -> chordOrNote.note != null)//
				.map(chordOrNote -> new ArrangementNote(chordOrNote.note))//
				.forEach(notes.list::add);
	}

	private void setChords(final ArrayList2<ChordOrNote> chordsAndNotes,
			final ArrayList2<ChordTemplate> chordTemplates) {
		chords = new CountedList<>();
		chordsAndNotes.stream()//
				.filter(chordOrNote -> chordOrNote.chord != null)//
				.map(chordOrNote -> new ArrangementChord(chordOrNote.chord,
						chordTemplates.get(chordOrNote.chord.chordId)))//
				.forEach(chords.list::add);
	}

	private void setHandShapes(final ArrayList2<ChordOrNote> chordsAndNotes, final ArrayList2<HandShape> handShapes) {
		final LinkedList<Chord> chordsForHandShapes = chordsAndNotes.stream()//
				.filter(chordOrNote -> chordOrNote.chord != null)//
				.map(chordOrNote -> chordOrNote.chord)//
				.collect(Collectors.toCollection(LinkedList::new));
		final ArrayList2<Chord> chordsWithoutHandShapes = new ArrayList2<>();
		this.handShapes = new CountedList<ArrangementHandShape>();
		for (final HandShape handShape : handShapes) {
			while (!chordsForHandShapes.isEmpty() && chordsForHandShapes.get(0).position() < handShape.position()) {
				chordsWithoutHandShapes.add(chordsForHandShapes.get(0));
				chordsForHandShapes.remove(0);
			}
			while (!chordsForHandShapes.isEmpty() && chordsForHandShapes.get(0).position() < handShape.endPosition()) {
				chordsForHandShapes.remove(0);
			}

			this.handShapes.list.add(new ArrangementHandShape(handShape));
		}

		this.handShapes.list.addAll(chordsWithoutHandShapes.map(ArrangementHandShape::new));
		this.handShapes.list.sort((a, b) -> Integer.compare(a.startTime, b.startTime));
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
			if (chordOrNote.position() > handShape.endTime) {
				continue;
			}

			if (chordOrNote.isChord()) {
				seekableArrangementChords.seekNextGreaterEqual(chordOrNote.chord.position());
				final ArrangementChord chord = seekableArrangementChords.getCurrent();
				final ChordTemplate chordTemplate = chordTemplates.get(chord.chordId);
				if (chord.chordNotes != null && !chord.chordNotes.isEmpty()) {
					continue;
				}

				chord.populateChordNotes(chordTemplate);
				continue;
			}

		}
	}
}
