package log.charter.io.rs.xml.song;

import java.util.LinkedList;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.Config;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.song.Chord;
import log.charter.song.Level;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

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

		levelChart.chordsAndNotes//
				.stream().filter(chordOrNote -> chordOrNote.note != null)//
				.map(chordOrNote -> chordOrNote.note)//
				.forEach(note -> note.sustain = note.sustain >= Config.minTailLength ? note.sustain : 0);

		notes = new CountedList<>();
		levelChart.chordsAndNotes.stream()//
				.filter(chordOrNote -> chordOrNote.note != null)//
				.map(chordOrNote -> new ArrangementNote(chordOrNote.note))//
				.forEach(notes.list::add);
		chords = new CountedList<>();

		levelChart.chordsAndNotes.stream()//
				.filter(chordOrNote -> chordOrNote.chord != null)//
				.map(chordOrNote -> new ArrangementChord(chordOrNote.chord,
						chordTemplates.get(chordOrNote.chord.chordId)))//
				.forEach(chords.list::add);
		fretHandMutes = new CountedList<>();
		anchors = new CountedList<>(levelChart.anchors.map(ArrangementAnchor::new));

		final LinkedList<Chord> chordsForHandShapes = levelChart.chordsAndNotes.stream()//
				.filter(chordOrNote -> chordOrNote.chord != null)//
				.map(chordOrNote -> chordOrNote.chord)//
				.collect(Collectors.toCollection(LinkedList::new));
		final ArrayList2<Chord> chordsWithoutHandShapes = new ArrayList2<>();
		handShapes = new CountedList<>(levelChart.handShapes.map(handShape -> {
			int chordId = 0;

			while (!chordsForHandShapes.isEmpty() && chordsForHandShapes.get(0).position < handShape.position) {
				chordsWithoutHandShapes.add(chordsForHandShapes.get(0));
				chordsForHandShapes.remove(0);
			}

			if (!chordsForHandShapes.isEmpty()) {
				chordId = chordsForHandShapes.get(0).chordId;
			}

			return new ArrangementHandShape(handShape, chordId);
		}));

		handShapes.list.addAll(chordsWithoutHandShapes.map(ArrangementHandShape::new));
		handShapes.list.sort((a, b) -> Integer.compare(a.startTime, b.startTime));
	}
}
