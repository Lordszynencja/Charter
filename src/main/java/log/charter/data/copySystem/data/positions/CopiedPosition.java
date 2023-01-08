package log.charter.data.copySystem.data.positions;

import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Beat;
import log.charter.song.notes.IPosition;

public abstract class CopiedPosition<T extends IPosition> {
	public static int findPositionForBeatPosition(final List<Beat> beats, final double beatPosition) {
		final int beatId = (int) beatPosition;
		final Beat beat = beats.get(beatId);
		if (beatId >= beats.size() - 1) {
			return beat.position();
		}

		final Beat nextBeat = beats.get(beatId + 1);

		return (int) (beat.position() + (nextBeat.position() - beat.position()) * (beatPosition % 1.0));
	}

	public static double findBeatPositionForPosition(final List<Beat> beats, final int position) {
		final int beatId = findLastIdBeforeEqual(beats, position);
		if (beatId >= beats.size() - 1) {
			return beatId;
		}

		final Beat beat = beats.get(beatId);
		final Beat nextBeat = beats.get(beatId + 1);

		return beatId + 1.0 * (position - beat.position()) / (nextBeat.position() - beat.position());
	}

	@XStreamAsAttribute
	public final double position;

	public CopiedPosition(final List<Beat> beats, final double basePositionInBeats, final T positionWithLength) {
		position = findBeatPositionForPosition(beats, positionWithLength.position()) - basePositionInBeats;
	}

	protected abstract T prepareValue();

	public T getValue(final List<Beat> beats, final double basePositionInBeats) {
		final T value = prepareValue();

		final double startBeatPosition = basePositionInBeats + position;

		if (startBeatPosition < 0 || startBeatPosition > beats.size() - 1) {
			return null;
		}

		value.position(findPositionForBeatPosition(beats, startBeatPosition));

		return value;
	}
}
