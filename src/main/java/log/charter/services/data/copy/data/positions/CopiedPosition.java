package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IVirtualPosition;

public abstract class CopiedPosition<T extends IVirtualPosition> {
	@XStreamAsAttribute
	public final int p;
	@XStreamAsAttribute
	public final FractionalPosition fp;

	public CopiedPosition(final ImmutableBeatsMap beats, final FractionalPosition basePosition, final T item) {
		this.p = item.positionAsPosition(beats).position() - basePosition.position(beats);
		fp = item.positionAsFraction(beats).fractionalPosition().add(basePosition.negate());
	}

	protected abstract T prepareValue();

	public T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		final T value = prepareValue();
		if (value == null) {
			return null;
		}

		if (convertFromBeats || value.isFractionalPosition()) {
			final FractionalPosition position = basePosition.add(fp);

			if (value.isFractionalPosition()) {
				value.asFraction().get().fractionalPosition(position);
			} else {
				value.asPosition().get().position(position.getPosition(beats));
			}
		} else if (value.isPosition()) {
			value.asPosition().get().position(basePosition.position(beats) + p);
		}

		return value;
	}
}
