package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPositionWithEnd;

public abstract class CopiedFractionalPositionWithEnd<T extends IFractionalPositionWithEnd>
		extends CopiedFractionalPosition<T> {
	@XStreamAsAttribute
	public final FractionalPosition ep;

	public CopiedFractionalPositionWithEnd(final FractionalPosition basePosition, final T position) {
		super(basePosition, position);
		ep = position.endPosition().add(basePosition.negate());
	}

	@Override
	public T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		final T value = super.getValue(beats, basePosition, convertFromBeats);
		if (value == null) {
			return null;
		}

		value.endPosition(ep.add(basePosition));

		return value;
	}
}
