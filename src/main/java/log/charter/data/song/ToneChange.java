package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.io.rsc.xml.converters.ToneChangeConverter;

@XStreamAlias("toneChange")
@XStreamConverter(ToneChangeConverter.class)
public class ToneChange implements IFractionalPosition {
	private FractionalPosition position;
	@XStreamAsAttribute
	public String toneName;

	public ToneChange() {
		position = new FractionalPosition(0);
	}

	public ToneChange(final FractionalPosition position) {
		this.position = position;
	}

	public ToneChange(final FractionalPosition position, final String toneName) {
		this.position = position;
		this.toneName = toneName;
	}

	public ToneChange(final ToneChange other) {
		position = other.position;
		toneName = other.toneName;
	}

	@Override
	public FractionalPosition fractionalPosition() {
		return position;
	}

	@Override
	public void fractionalPosition(final FractionalPosition newPosition) {
		position = newPosition;
	}
}
