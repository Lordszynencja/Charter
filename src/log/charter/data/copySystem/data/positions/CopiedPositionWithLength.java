package log.charter.data.copySystem.data.positions;

import java.util.List;

import log.charter.song.Beat;
import log.charter.song.notes.IPositionWithLength;

public abstract class CopiedPositionWithLength<T extends IPositionWithLength> extends CopiedPosition<T> {
	public final double length;

	public CopiedPositionWithLength(final List<Beat> beats, final double basePositionInBeats,
			final T positionWithLength) {
		super(beats, basePositionInBeats, positionWithLength);
		length = findBeatPositionForPosition(beats, positionWithLength.endPosition()) - position - basePositionInBeats;
	}

	@Override
	protected abstract T prepareValue();

	@Override
	public T getValue(final List<Beat> beats, final double basePositionInBeats) {
		final T value = super.getValue(beats, basePositionInBeats);
		if (value == null) {
			return null;
		}

		double endPositionInBeats = basePositionInBeats + position + length;
		if (endPositionInBeats > beats.size() - 1) {
			endPositionInBeats = beats.size() - 1;
		}

		value.length(findPositionForBeatPosition(beats, endPositionInBeats) - value.position());

		return value;
	}
}
