package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedSound")
public class CopiedSoundPosition extends CopiedPositionWithLength<ChordOrNote> {
	public final ChordOrNote sound;

	public CopiedSoundPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final ChordOrNote sound) {
		super(beats, basePosition, sound);
		this.sound = sound;
	}

	@Override
	protected ChordOrNote prepareValue() {
		return ChordOrNote.from(sound);
	}
}
