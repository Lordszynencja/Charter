package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.io.rsc.xml.converters.FHPConverter;

@XStreamAlias("anchor")
@XStreamConverter(FHPConverter.class)
public class FHP implements IFractionalPosition {

	private FractionalPosition position;
	public int fret = 1;
	public int width = 4;

	public FHP() {
		position = new FractionalPosition();
	}

	public FHP(final FractionalPosition position, final int fret) {
		this.position = position;
		this.fret = fret;
	}

	public FHP(final FractionalPosition position) {
		this.position = position;
	}

	public FHP(final FractionalPosition position, final int fret, final int width) {
		this.position = position;
		this.fret = fret;
		this.width = width;
	}

	public FHP(final FHP other) {
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
