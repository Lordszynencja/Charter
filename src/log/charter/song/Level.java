package log.charter.song;

import java.util.List;

import log.charter.data.managers.selection.ChordOrNote;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementLevel;
import log.charter.io.rs.xml.song.ArrangementNote;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.Pair;

public class Level {
	public static HashMap2<Integer, Level> fromArrangementLevels(final List<ArrangementLevel> arrangementLevels) {
		return new ArrayList2<>(arrangementLevels)
				.toMap(arrangementLevel -> new Pair<>(arrangementLevel.difficulty, new Level(arrangementLevel)));
	}

	public ArrayList2<Anchor> anchors = new ArrayList2<>();
	public ArrayList2<ChordOrNote> chordsAndNotes = new ArrayList2<>();
	public ArrayList2<HandShape> handShapes = new ArrayList2<>();

	public Level() {
	}

	private Level(final ArrangementLevel level) {
		anchors = level.anchors.list.map(Anchor::new);
		handShapes = level.handShapes.list.map(HandShape::new);

		for (final ArrangementChord arrangementChord : level.chords.list) {
			chordsAndNotes.add(new ChordOrNote(new Chord(arrangementChord)));
		}
		for (final ArrangementNote arrangementNote : level.notes.list) {
			chordsAndNotes.add(new ChordOrNote(new Note(arrangementNote)));
		}
		chordsAndNotes.sort(null);
	}
}
