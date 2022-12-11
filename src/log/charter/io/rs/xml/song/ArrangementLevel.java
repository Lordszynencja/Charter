package log.charter.io.rs.xml.song;

import java.util.LinkedList;

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
	public static ArrayList2<ArrangementLevel> fromLevels(final HashMap2<Integer, Level> levels) {
		final ArrayList2<ArrangementLevel> arrangementLevels = levels.map(ArrangementLevel::new);

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

	private ArrangementLevel(final int difficulty, final Level levelChart) {
		this.difficulty = difficulty;

		levelChart.notes.forEach(note -> note.sustain = note.sustain >= Config.minTailLength ? note.sustain : 0);

		notes = new CountedList<>(levelChart.notes.map(ArrangementNote::new));
		chords = new CountedList<>(levelChart.chords.map(ArrangementChord::new));
		fretHandMutes = new CountedList<>();
		anchors = new CountedList<>(levelChart.anchors.map(ArrangementAnchor::new));

		final LinkedList<Chord> chordsForHandShapes = new LinkedList<>(levelChart.chords);
		final ArrayList2<Chord> chordsWithoutHandShapes = new ArrayList2<>(levelChart.chords);
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
