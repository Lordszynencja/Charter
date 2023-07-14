package log.charter.io.rs.xml.song;

import static log.charter.song.notes.IPosition.findLastBeforeEqual;

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

		setChordsAndNotes(chordTemplates, level.chordsAndNotes, level.handShapes);

		fretHandMutes = new CountedList<>();
		anchors = new CountedList<>(level.anchors.map(ArrangementAnchor::new));
		handShapes = new CountedList<ArrangementHandShape>(level.handShapes.map(ArrangementHandShape::new));
	}

	private void setChordsAndNotes(final ArrayList2<ChordTemplate> chordTemplates,
			final ArrayList2<ChordOrNote> chordsAndNotes, final ArrayList2<HandShape> handShapes) {
		notes = new CountedList<>();
		chords = new CountedList<>();

		for (int i = 0; i < chordsAndNotes.size(); i++) {
			final ChordOrNote sound = chordsAndNotes.get(i);
			if (sound.isNote()) {
				notes.list.add(new ArrangementNote(sound.note));
				continue;
			}

			final Chord chord = sound.chord;
			final ChordTemplate chordTemplate = chordTemplates.get(chord.templateId());
			final HandShape handShape = findLastBeforeEqual(handShapes, chord.position());
			final boolean forceAddNotes = handShape != null//
					&& (i == 0//
							|| handShape.templateId != chord.templateId()//
							|| (chordsAndNotes.get(i - 1).position() > handShape.position()));
			final int nextPosition = i + 1 < chordsAndNotes.size() ? chordsAndNotes.get(i + 1).position()
					: chord.position() + 100;
			final ArrangementChord arrangementChord = new ArrangementChord(chord, chordTemplate, nextPosition,
					forceAddNotes);

			if (chord.splitIntoNotes) {
				arrangementChord.chordNotes.stream().map(ArrangementNote::new).forEach(notes.list::add);
			} else {
				chords.list.add(arrangementChord);
			}
		}
	}
}
