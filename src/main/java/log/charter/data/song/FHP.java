package log.charter.data.song;

import static java.lang.Math.min;

import java.util.Map.Entry;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.config.values.InstrumentConfig;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.io.rsc.xml.converters.FHPConverter;

@XStreamAlias("anchor")
@XStreamConverter(FHPConverter.class)
public class FHP implements IFractionalPosition {
	public static Integer getFhpFretForChord(final ChordTemplate template) {
		Integer lowestFret = null;
		Integer lowestFinger = null;
		for (final Entry<Integer, Integer> finger : template.fingers.entrySet()) {
			if (finger.getValue() == null) {
				continue;
			}

			final Integer fret = template.frets.get(finger.getKey());
			if (fret == null || fret == 0) {
				continue;
			}

			if (lowestFret == null || lowestFret > fret || (lowestFret == fret && lowestFinger < finger.getValue())) {
				lowestFret = fret;
				lowestFinger = finger.getValue();
			}
		}

		if (lowestFret == null) {
			return null;
		}
		if (lowestFinger == 5) {
			return lowestFret;
		}
		return min(InstrumentConfig.frets - 3, lowestFret - lowestFinger + 1);
	}

	private FractionalPosition position;
	public int fret = 1;
	public int width = 4;

	public FHP() {
		position = new FractionalPosition();
	}

	public FHP(final FractionalPosition position) {
		this.position = position;
	}

	public FHP(final FractionalPosition position, final int fret) {
		this.position = position;
		this.fret = fret;
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
