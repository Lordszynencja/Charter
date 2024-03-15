package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IFractionalPosition;

public abstract class CopiedFractionalPosition<T extends IFractionalPosition> extends Copied<T> {
	@XStreamAsAttribute
	public final FractionalPosition fp;

	public CopiedFractionalPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final T item) {
		fp = item.fractionalPosition().add(basePosition.negate());
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

		value.asFraction().get().fractionalPosition(basePosition.add(fp));

		return value;
	}
}
