package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IConstantFractionalPosition;
import log.charter.data.song.position.IFractionalPosition;
import log.charter.io.rsc.xml.converters.AnchorConverter;

@XStreamAlias("anchor")
@XStreamConverter(AnchorConverter.class)
public class Anchor implements IFractionalPosition {

	private FractionalPosition fractionalPosition;
	public int fret = 1;
	public int width = 4;

	public Anchor() {
		fractionalPosition = new FractionalPosition(0);
	}

	public Anchor(final IConstantFractionalPosition fractionalPosition, final int fret) {
		this.fractionalPosition = fractionalPosition.fractionalPosition();
		this.fret = fret;
	}

	public Anchor(final IConstantFractionalPosition fractionalPosition) {
		this.fractionalPosition = fractionalPosition.fractionalPosition();
	}

	public Anchor(final IConstantFractionalPosition fractionalPosition, final int fret, final int width) {
		this.fractionalPosition = fractionalPosition.fractionalPosition();
		this.fret = fret;
		this.width = width;
	}

	public Anchor(final Anchor other) {
		fractionalPosition = other.fractionalPosition;
		fret = other.fret;
		width = other.width;
	}

	public int topFret() {
		return fret + width - 1;
	}

	@Override
	public FractionalPosition fractionalPosition() {
		return fractionalPosition;
	}

	@Override
	public void fractionalPosition(final FractionalPosition newPosition) {
		if (newPosition == null) {
			throw new IllegalArgumentException("Can't set position to null");
		}

		fractionalPosition = newPosition;
	}
}
