package log.charter.services.data.copy.data;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.position.IPosition;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedPosition;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.collections.ArrayList2;

public interface ICopyData {
	static <T extends IPosition, V extends CopiedPosition<T>> void simplePaste(final ChartData chartData,
			final SelectionManager selectionManager, final PositionType type, final int time,
			final ArrayList2<V> positionsToPaste, final boolean convertFromBeats) {
		final BeatsMap beatsMap = chartData.songChart.beatsMap;
		final ArrayList2<T> positions = type.getPositions(chartData);
		final double basePositionInBeats = beatsMap.getPositionInBeats(time);
		final Set<Integer> positionsToSelect = new HashSet<>(positionsToPaste.size());

		for (final V copiedPosition : positionsToPaste) {
			try {
				final T value = copiedPosition.getValue(beatsMap, time, basePositionInBeats, convertFromBeats);
				if (value != null) {
					positions.add(value);
					positionsToSelect.add(value.position());
				}
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		}

		positions.sort(null);
		selectionManager.addSelectionForPositions(type, positionsToSelect);
	}

	public boolean isEmpty();

	public void paste(ChartData chartData, SelectionManager selectionManager, int time, boolean convertFromBeats);
}
