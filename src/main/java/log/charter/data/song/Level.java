package log.charter.data.song;

import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.util.CollectionUtils;
import log.charter.util.data.Fraction;

@XStreamAlias("level")
@XStreamInclude({ FHP.class, ChordOrNote.class, HandShape.class })
public class Level {
	public static final Level dummy = new Level();

	private static void addDummyNote(final int string, final int fret) {
		dummy.sounds.add(ChordOrNote.from(new Note(new FractionalPosition(new Fraction(1, 2)), string, fret)));
	}

	static {
		dummy.fhps.add(new FHP(new FractionalPosition(new Fraction(1, 100)), 1, 23));

		// E
		addDummyNote(0, 1);
		addDummyNote(1, 1);
		addDummyNote(2, 1);
		addDummyNote(3, 1);
		addDummyNote(4, 1);
		addDummyNote(5, 1);
		addDummyNote(0, 2);
		addDummyNote(2, 2);
		addDummyNote(5, 2);
		addDummyNote(0, 3);
		addDummyNote(2, 3);
		addDummyNote(5, 3);

		// R
		addDummyNote(0, 5);
		addDummyNote(1, 5);
		addDummyNote(2, 5);
		addDummyNote(3, 5);
		addDummyNote(4, 5);
		addDummyNote(5, 5);
		addDummyNote(0, 6);
		addDummyNote(2, 6);
		addDummyNote(3, 6);
		addDummyNote(1, 7);
		addDummyNote(4, 7);
		addDummyNote(5, 8);

		// R
		addDummyNote(0, 10);
		addDummyNote(1, 10);
		addDummyNote(2, 10);
		addDummyNote(3, 10);
		addDummyNote(4, 10);
		addDummyNote(5, 10);
		addDummyNote(0, 11);
		addDummyNote(2, 11);
		addDummyNote(3, 11);
		addDummyNote(1, 12);
		addDummyNote(4, 12);
		addDummyNote(5, 13);

		// O
		addDummyNote(1, 15);
		addDummyNote(2, 15);
		addDummyNote(3, 15);
		addDummyNote(4, 15);
		addDummyNote(0, 16);
		addDummyNote(5, 16);
		addDummyNote(0, 17);
		addDummyNote(5, 17);
		addDummyNote(1, 18);
		addDummyNote(2, 18);
		addDummyNote(3, 18);
		addDummyNote(4, 18);

		// R
		addDummyNote(0, 20);
		addDummyNote(1, 20);
		addDummyNote(2, 20);
		addDummyNote(3, 20);
		addDummyNote(4, 20);
		addDummyNote(5, 20);
		addDummyNote(0, 21);
		addDummyNote(2, 21);
		addDummyNote(3, 21);
		addDummyNote(1, 22);
		addDummyNote(4, 22);
		addDummyNote(5, 23);
	}

	@XStreamAlias("anchors")
	public List<FHP> fhps = new ArrayList<>();
	public List<ChordOrNote> sounds = new ArrayList<>();
	public List<HandShape> handShapes = new ArrayList<>();

	public Level() {
	}

	public Level(final Level other) {
		fhps = CollectionUtils.map(other.fhps, FHP::new);
		sounds = CollectionUtils.map(other.sounds, ChordOrNote::from);
		handShapes = CollectionUtils.map(other.handShapes, HandShape::new);
	}

	public boolean shouldChordShowNotes(final ImmutableBeatsMap beats, final int id) {
		final ChordOrNote sound = sounds.get(id);
		if (sound.isNote()) {
			return true;
		}

		final Chord chord = sound.chord();
		final HandShape handShape = lastBeforeEqual(handShapes, chord).find();
		if (handShape == null || handShape.templateId == null) {
			return true;
		}
		if (handShape.templateId != chord.templateId()) {
			return true;
		}

		for (int j = id - 1; j >= 0; j--) {
			final ChordOrNote previousSound = sounds.get(j);
			if (previousSound.isNote()//
					|| previousSound.chord().fullyMuted()) {
				continue;
			}
			if (previousSound.chord().templateId() != handShape.templateId) {
				return true;
			}
			if (previousSound.position().compareTo(handShape.position()) < 0) {
				break;
			}

			return false;
		}

		return true;
	}
}
