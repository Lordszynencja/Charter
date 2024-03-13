package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.vocals.Vocal;

@XStreamAlias("copiedVocal")
public class CopiedVocalPosition extends CopiedPositionWithLength<Vocal> {
	@XStreamAsAttribute
	public final String text;

	public CopiedVocalPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final Vocal vocal) {
		super(beats, basePosition, vocal);
		text = vocal.lyric;
	}

	@Override
	protected Vocal prepareValue() {
		return new Vocal(text);
	}
}
