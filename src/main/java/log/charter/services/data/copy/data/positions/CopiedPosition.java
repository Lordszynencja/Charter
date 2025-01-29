package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.time.IPosition;

public abstract class CopiedPosition<T extends IPosition> implements Copied<T> {
	@XStreamAsAttribute
	public final double p;
	@XStreamAsAttribute
	public final FractionalPosition fp;

	public CopiedPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition, final T item) {
		this.p = item.position() - basePosition.position(beats);
		fp = item.toFraction(beats).position().add(basePosition.negate());
	}

	@Override
	public T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		final T value = Copied.super.getValue(beats, basePosition, convertFromBeats);
		if (value == null) {
			return null;
		}

		if (convertFromBeats || value.isFraction()) {
			final FractionalPosition position = basePosition.add(fp);

			value.position(position.getPosition(beats));
		} else {
			value.position(basePosition.position(beats) + p);
		}

		return value;
	}
}
