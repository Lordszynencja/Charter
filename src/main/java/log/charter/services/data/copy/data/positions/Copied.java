package log.charter.services.data.copy.data.positions;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;

public abstract class Copied<T> {
	protected abstract T prepareValue();

	public T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		return prepareValue();
	}
}
