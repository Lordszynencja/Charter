package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IPositionWithLength;

public abstract class CopiedPositionWithLength<T extends IPositionWithLength> extends CopiedPosition<T> {
	@XStreamAsAttribute
	public final int l;
	@XStreamAsAttribute
	public final FractionalPosition fl;

	public CopiedPositionWithLength(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final T positionWithLength) {
		super(beats, basePosition, positionWithLength);
		l = positionWithLength.length();
		fl = positionWithLength.endPosition().toFraction(beats).position().add(super.fp.negate());
	}

	@Override
	public T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		final T value = super.getValue(beats, basePosition, convertFromBeats);
		if (value == null) {
			return null;
		}

		if (convertFromBeats) {
			final FractionalPosition endPosition = basePosition.add(fp).add(fl);

			value.endPosition(endPosition.getPosition(beats));
		} else if (value.isPosition()) {
			value.endPosition(value.position() + l);
		}

		return value;
	}
}
