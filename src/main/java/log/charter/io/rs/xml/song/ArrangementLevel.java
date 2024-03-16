package log.charter.io.rs.xml.song;

import static log.charter.util.CollectionUtils.map;

import java.math.BigDecimal;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.Anchor;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;
import log.charter.util.CollectionUtils;

@XStreamAlias("level")
public class ArrangementLevel {
	public static List<ArrangementLevel> fromLevels(final ImmutableBeatsMap beats, final List<Level> levels,
			final List<ChordTemplate> chordTemplates) {
		final List<ArrangementLevel> arrangementLevels = CollectionUtils.mapWithId(levels,
				(difficulty, level) -> new ArrangementLevel(beats, difficulty, level, chordTemplates));

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

	private ArrangementAnchor anchor(final ImmutableBeatsMap beats, final Anchor anchor) {
		return new ArrangementAnchor(anchor.position(beats), anchor.fret, new BigDecimal(anchor.width));
	}

	private ArrangementHandShape handShape(final ImmutableBeatsMap beats, final HandShape handShape) {
		return new ArrangementHandShape(handShape.position(beats), handShape.endPosition(beats),
				handShape.templateId == null ? 0 : handShape.templateId);
	}

	private ArrangementLevel(final ImmutableBeatsMap beats, final int difficulty, final Level level,
			final List<ChordTemplate> chordTemplates) {
		this.difficulty = difficulty;

		setChordsAndNotes(beats, level, chordTemplates);

		fretHandMutes = new CountedList<>();
		anchors = new CountedList<>(map(level.anchors, a -> anchor(beats, a)));
		handShapes = new CountedList<>(map(level.handShapes, h -> handShape(beats, h)));
	}

	private void setChordsAndNotes(final ImmutableBeatsMap beats, final Level level,
			final List<ChordTemplate> chordTemplates) {
		notes = new CountedList<>();
		chords = new CountedList<>();
		final List<ChordOrNote> chordsAndNotes = level.sounds;

		for (int i = 0; i < chordsAndNotes.size(); i++) {
			final ChordOrNote sound = chordsAndNotes.get(i);
			if (sound.isNote()) {
				notes.list.add(new ArrangementNote(sound.note()));
				continue;
			}

			final Chord chord = sound.chord();
			final ChordTemplate chordTemplate = chordTemplates.get(chord.templateId());
			final boolean forceAddNotes = level.shouldChordShowNotes(beats, i);
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
