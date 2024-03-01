package log.charter.data.copySystem.data;

import log.charter.data.ChartData;
import log.charter.data.copySystem.data.positions.CopiedPosition;
import log.charter.io.Logger;
import log.charter.song.BeatsMap;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public interface ICopyData {
	static <T extends IPosition, V extends CopiedPosition<T>> void simplePaste(final BeatsMap beatsMap, final int time,
			final ArrayList2<T> positions, final ArrayList2<V> positionsToPaste, final boolean convertFromBeats) {
		final double basePositionInBeats = beatsMap.getPositionInBeats(time);

		for (final V copiedPosition : positionsToPaste) {
			try {
				final T value = copiedPosition.getValue(beatsMap, time, basePositionInBeats, convertFromBeats);
				if (value != null) {
					positions.add(value);
				}
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		}

		positions.sort(null);
	}

	public boolean isEmpty();

	public void paste(int time, ChartData data, boolean convertFromBeats);
}
