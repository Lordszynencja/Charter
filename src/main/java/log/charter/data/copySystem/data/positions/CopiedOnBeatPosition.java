package log.charter.data.copySystem.data.positions;

import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.song.Beat;
import log.charter.song.OnBeat;
import log.charter.util.CollectionUtils.ArrayList2;

public abstract class CopiedOnBeatPosition<T extends OnBeat> {
	@XStreamAsAttribute
	public final int beatId;

	public CopiedOnBeatPosition(final ArrayList2<Beat> beats, final int baseBeat, final T onBeat) {
		beatId = findLastIdBeforeEqual(beats, onBeat.beat.position()) - baseBeat;
	}

	protected abstract T createValue();

	public T getValue(final ArrayList2<Beat> beats, final int baseBeatId) {
		final T value = createValue();

		final int valueBeatId = baseBeatId + beatId;
		if (valueBeatId < 0 || valueBeatId >= beats.size()) {
			return null;
		}

		value.beat = beats.get(baseBeatId + beatId);

		return value;
	}
}
