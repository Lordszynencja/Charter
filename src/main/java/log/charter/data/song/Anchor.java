package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.io.rsc.xml.converters.AnchorConverter;

@XStreamAlias("anchor")
@XStreamConverter(AnchorConverter.class)
public class Anchor implements IFractionalPosition {

	private FractionalPosition position;
	public int fret = 1;
	public int width = 4;

	public Anchor() {
		position = new FractionalPosition();
	}

	public Anchor(final FractionalPosition position, final int fret) {
		this.position = position;
		this.fret = fret;
	}

	public Anchor(final FractionalPosition position) {
		this.position = position;
	}

	public Anchor(final FractionalPosition position, final int fret, final int width) {
		this.position = position;
		this.fret = fret;
		this.width = width;
	}

	public Anchor(final Anchor other) {
		position = other.position;
		fret = other.fret;
		width = other.width;
	}

	public int topFret() {
		return fret + width - 1;
	}

	@Override
	public FractionalPosition position() {
		return position;
	}

	@Override
	public void position(final FractionalPosition newPosition) {
		if (newPosition == null) {
			throw new IllegalArgumentException("Can't set position to null");
		}

		position = newPosition;
	}
}
