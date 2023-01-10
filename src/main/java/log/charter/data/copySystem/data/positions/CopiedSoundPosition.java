package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.BeatsMap;
import log.charter.song.notes.ChordOrNote;

@XStreamAlias("copiedSound")
public class CopiedSoundPosition extends CopiedPositionWithLength<ChordOrNote> {
	public final ChordOrNote sound;

	public CopiedSoundPosition(final BeatsMap beatsMap, final double baseBeatPosition, final ChordOrNote sound) {
		super(beatsMap, baseBeatPosition, sound);
		this.sound = sound;
	}

	@Override
	protected ChordOrNote prepareValue() {
		return new ChordOrNote(sound);
	}
}
