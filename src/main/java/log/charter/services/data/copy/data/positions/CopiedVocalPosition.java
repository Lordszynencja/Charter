package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.BeatsMap;
import log.charter.song.vocals.Vocal;

@XStreamAlias("copiedVocal")
public class CopiedVocalPosition extends CopiedPositionWithLength<Vocal> {
	@XStreamAsAttribute
	public final String text;

	public CopiedVocalPosition(final BeatsMap beatsMap, final int basePosition, final double basePositionInBeats,
			final Vocal vocal) {
		super(beatsMap, basePosition, basePositionInBeats, vocal);
		text = vocal.lyric;
	}

	@Override
	protected Vocal prepareValue() {
		return new Vocal(text);
	}
}
