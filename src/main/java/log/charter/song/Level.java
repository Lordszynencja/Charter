package log.charter.song;

import static log.charter.song.notes.IConstantPosition.findLastBeforeEquals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("level")
@XStreamInclude({ Anchor.class, ChordOrNote.class, HandShape.class })
public class Level {
	public ArrayList2<Anchor> anchors = new ArrayList2<>();
	public ArrayList2<ChordOrNote> sounds = new ArrayList2<>();
	public ArrayList2<HandShape> handShapes = new ArrayList2<>();

	public Level() {
	}

	public boolean shouldChordShowNotes(final int id) {
		final ChordOrNote sound = sounds.get(id);
		if (sound.isNote()) {
			return true;
		}

		final Chord chord = sound.chord();
		final HandShape handShape = findLastBeforeEquals(handShapes, chord.position());
		if (handShape == null) {
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
			if (previousSound.position() < handShape.position()) {
				break;
			}

			return false;
		}

		return true;
	}
}
