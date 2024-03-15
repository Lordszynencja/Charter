package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.song.vocals.Vocal.VocalFlag;

@XStreamAlias("copiedVocal")
public class CopiedVocalPosition extends CopiedFractionalPositionWithLength<Vocal> {
	@XStreamAsAttribute
	public final String text;
	@XStreamAsAttribute
	public final VocalFlag flag;

	public CopiedVocalPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final Vocal vocal) {
		super(beats, basePosition, vocal);
		text = vocal.text();
		flag = vocal.flag();
	}

	@Override
	protected Vocal prepareValue() {
		return new Vocal(text);
	}
}
