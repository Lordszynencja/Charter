package log.charter.data.copySystem.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.BeatsMap;
import log.charter.song.notes.IPositionWithLength;

public abstract class CopiedPositionWithLength<T extends IPositionWithLength> extends CopiedPosition<T> {
	@XStreamAsAttribute
	public final double length;

	public CopiedPositionWithLength(final BeatsMap beatsMap, final double basePositionInBeats,
			final T positionWithLength) {
		super(beatsMap, basePositionInBeats, positionWithLength);
		length = beatsMap.getPositionInBeats(positionWithLength.endPosition()) - position - basePositionInBeats;
	}

	@Override
	protected abstract T prepareValue();

	@Override
	public T getValue(final BeatsMap beatsMap, final double basePositionInBeats) {
		final T value = super.getValue(beatsMap, basePositionInBeats);
		if (value == null) {
			return null;
		}

		double endPositionInBeats = basePositionInBeats + position + length;
		if (endPositionInBeats > beatsMap.beats.size() - 1) {
			endPositionInBeats = beatsMap.beats.size() - 1;
		}

		value.length(beatsMap.getPositionForPositionInBeats(endPositionInBeats) - value.position());

		return value;
	}
}
