package log.charter.data.copySystem.data.positions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.song.Beat;
import log.charter.song.notes.ChordOrNote;

@XStreamAlias("copiedSound")
public class CopiedSoundPosition extends CopiedPositionWithLength<ChordOrNote> {
	public final ChordOrNote sound;

	public CopiedSoundPosition(final List<Beat> beats, final double baseBeatPosition, final ChordOrNote sound) {
		super(beats, baseBeatPosition, sound);
		this.sound = sound;
	}

	@Override
	protected ChordOrNote prepareValue() {
		return new ChordOrNote(sound);
	}
}
