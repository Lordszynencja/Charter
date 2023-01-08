package log.charter.data.copySystem.data.positions;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Beat;
import log.charter.song.ToneChange;

@XStreamAlias("copiedToneChange")
public class CopiedToneChangePosition extends CopiedPosition<ToneChange> {
	@XStreamAsAttribute
	public final String toneName;

	public CopiedToneChangePosition(final List<Beat> beats, final double baseBeatPosition,
			final ToneChange toneChange) {
		super(beats, baseBeatPosition, toneChange);
		toneName = toneChange.toneName;
	}

	@Override
	protected ToneChange prepareValue() {
		final ToneChange toneChange = new ToneChange();
		toneChange.toneName = toneName;

		return toneChange;
	}
}
