package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ToneChange;
import log.charter.data.song.position.FractionalPosition;

@XStreamAlias("copiedToneChange")
public class CopiedToneChange extends CopiedFractionalPosition<ToneChange> {
	@XStreamAsAttribute
	public final String toneName;

	public CopiedToneChange(final FractionalPosition basePosition, final ToneChange toneChange) {
		super(basePosition, toneChange);
		toneName = toneChange.toneName;
	}

	@Override
	public ToneChange prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		final ToneChange toneChange = new ToneChange();
		toneChange.toneName = toneName;

		return toneChange;
	}
}
