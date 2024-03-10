package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.BeatsMap;
import log.charter.song.notes.ChordOrNote;

@XStreamAlias("copiedSound")
public class CopiedSoundPosition extends CopiedPositionWithLength<ChordOrNote> {
	public final ChordOrNote sound;

	public CopiedSoundPosition(final BeatsMap beatsMap, final int basePosition, final double baseBeatPosition,
			final ChordOrNote sound) {
		super(beatsMap, basePosition, baseBeatPosition, sound);
		this.sound = sound;
	}

	@Override
	protected ChordOrNote prepareValue() {
		return ChordOrNote.from(sound);
	}
}
