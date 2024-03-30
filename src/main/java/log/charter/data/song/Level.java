package log.charter.data.song;

import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;

@XStreamAlias("level")
@XStreamInclude({ Anchor.class, ChordOrNote.class, HandShape.class })
public class Level {
	public List<Anchor> anchors = new ArrayList<>();
	public List<ChordOrNote> sounds = new ArrayList<>();
	public List<HandShape> handShapes = new ArrayList<>();

	public Level() {
	}

	public boolean shouldChordShowNotes(final ImmutableBeatsMap beats, final int id) {
		final ChordOrNote sound = sounds.get(id);
		if (sound.isNote()) {
			return true;
		}

		final Chord chord = sound.chord();
		final HandShape handShape = lastBeforeEqual(handShapes, chord).find();
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
			if (previousSound.position().compareTo(handShape.position()) < 0) {
				break;
			}

			return false;
		}

		return true;
	}
}
