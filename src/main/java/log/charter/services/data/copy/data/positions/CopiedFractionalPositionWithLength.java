package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IFractionalPositionWithEnd;

public abstract class CopiedFractionalPositionWithLength<T extends IFractionalPositionWithEnd>
		extends CopiedFractionalPosition<T> {
	@XStreamAsAttribute
	public final FractionalPosition ep;

	public CopiedFractionalPositionWithLength(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final T position) {
		super(beats, basePosition, position);
		ep = position.fractionalEndPosition();
	}

	@Override
	protected abstract T prepareValue();

	@Override
	public T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		final T value = super.getValue(beats, basePosition, convertFromBeats);
		if (value == null) {
			return null;
		}

		value.fractionalEndPosition(basePosition.add(ep));

		return value;
	}
}
