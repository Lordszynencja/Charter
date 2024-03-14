package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IConstantFractionalPosition;
import log.charter.data.song.position.IFractionalPosition;

@XStreamAlias("anchor")
public class Anchor implements IFractionalPosition {

	@XStreamAsAttribute
	private FractionalPosition fractionalPosition;
	@XStreamAsAttribute
	private int position;
	@XStreamAsAttribute
	public int fret = 1;
	@XStreamAsAttribute
	public int width = 4;

	public Anchor() {
		fractionalPosition = new FractionalPosition(0);
		position = 1;
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
		position = other.position;
		fret = other.fret;
		width = other.width;
	}

	public int topFret() {
		return fret + width - 1;
	}

	public int oldPosition() {
		return position;
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
