package log.charter.data.copySystem.data;

import static log.charter.data.copySystem.data.positions.CopiedPosition.findBeatPositionForPosition;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedOnBeatPosition;
import log.charter.data.copySystem.data.positions.CopiedPosition;
import log.charter.io.Logger;
import log.charter.song.Beat;
import log.charter.song.OnBeat;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public interface ICopyData {
	static <T extends IPosition, V extends CopiedPosition<T>> void simplePaste(final ArrayList2<Beat> beats,
			final int time, final ArrayList2<T> positions, final ArrayList2<V> positionsToPaste) {
		final double basePositionInBeats = findBeatPositionForPosition(beats, time);

		for (final V copiedPosition : positionsToPaste) {
			try {
				positions.add(copiedPosition.getValue(beats, basePositionInBeats));
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		}

		positions.sort(null);
	}

	static <T extends OnBeat, V extends CopiedOnBeatPosition<T>> void simplePasteOnBeat(final ArrayList2<Beat> beats,
			final int time, final ArrayList2<T> positions, final ArrayList2<V> positionsToPaste) {
		final int baseBeatId = findLastIdBeforeEqual(beats, time);

		for (final V copiedPosition : positionsToPaste) {
			try {
				positions.add(copiedPosition.getValue(beats, baseBeatId));
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		}

		positions.sort(null);
	}

	public boolean isEmpty();

	public void paste(ChartData data);
}
