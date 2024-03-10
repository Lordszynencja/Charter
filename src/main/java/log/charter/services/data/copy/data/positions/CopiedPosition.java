package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.BeatsMap;
import log.charter.data.song.notes.IPosition;

public abstract class CopiedPosition<T extends IPosition> {

	@XStreamAsAttribute
	public final int position;
	@XStreamAsAttribute
	public final double positionInBeats;

	public CopiedPosition(final BeatsMap beatsMap, final int basePosition, final double basePositionInBeats,
			final T position) {
		this.position = position.position() - basePosition;
		positionInBeats = beatsMap.getPositionInBeats(position.position()) - basePositionInBeats;
	}

	protected abstract T prepareValue();

	public T getValue(final BeatsMap beatsMap, final int basePosition, final double basePositionInBeats,
			final boolean convertFromBeats) {
		final T value = prepareValue();

		if (convertFromBeats) {
			final double startBeatPosition = basePositionInBeats + positionInBeats;

			if (startBeatPosition < 0 || startBeatPosition > beatsMap.beats.size() - 1) {
				return null;
			}

			value.position(beatsMap.getPositionForPositionInBeats(startBeatPosition));
		} else {
			value.position(basePosition + position);
		}

		return value;
	}
}
