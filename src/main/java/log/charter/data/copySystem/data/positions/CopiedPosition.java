package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.BeatsMap;
import log.charter.song.notes.IPosition;

public abstract class CopiedPosition<T extends IPosition> {

	@XStreamAsAttribute
	public final double position;

	public CopiedPosition(final BeatsMap beatsMap, final double basePositionInBeats, final T positionWithLength) {
		position = beatsMap.getPositionInBeats(positionWithLength.position()) - basePositionInBeats;
	}

	protected abstract T prepareValue();

	public T getValue(final BeatsMap beatsMap, final double basePositionInBeats) {
		final T value = prepareValue();

		final double startBeatPosition = basePositionInBeats + position;

		if (startBeatPosition < 0 || startBeatPosition > beatsMap.beats.size() - 1) {
			return null;
		}

		value.position(beatsMap.getPositionForPositionInBeats(startBeatPosition));

		return value;
	}
}
