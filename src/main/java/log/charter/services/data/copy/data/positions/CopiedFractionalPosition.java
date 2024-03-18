package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;

public abstract class CopiedFractionalPosition<T extends IFractionalPosition> implements Copied<T> {
	@XStreamAsAttribute
	public final FractionalPosition fp;

	public CopiedFractionalPosition(final FractionalPosition basePosition, final T item) {
		fp = item.position().add(basePosition.negate());
	}

	@Override
	public T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		final T value = Copied.super.getValue(beats, basePosition, convertFromBeats);
		if (value == null) {
			return null;
		}

		value.asFraction().position(basePosition.add(fp));

		return value;
	}
}
