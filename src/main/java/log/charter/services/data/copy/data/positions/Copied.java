package log.charter.services.data.copy.data.positions;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;

public interface Copied<T> {
	abstract T prepareValue(ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats);

	default T getValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
			final boolean convertFromBeats) {
		return prepareValue(beats, basePosition, convertFromBeats);
	}
}
