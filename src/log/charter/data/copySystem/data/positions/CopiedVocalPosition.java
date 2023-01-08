package log.charter.data.copySystem.data.positions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Beat;
import log.charter.song.vocals.Vocal;

@XStreamAlias("copiedVocal")
public class CopiedVocalPosition extends CopiedPositionWithLength<Vocal> {
	@XStreamAsAttribute
	public final String text;

	public CopiedVocalPosition(final List<Beat> beats, final double basePositionInBeats, final Vocal vocal) {
		super(beats, basePositionInBeats, vocal);
		text = vocal.lyric;
	}

	@Override
	protected Vocal prepareValue() {
		return new Vocal(text);
	}
}
