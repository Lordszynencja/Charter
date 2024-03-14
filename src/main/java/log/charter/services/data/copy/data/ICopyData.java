package log.charter.services.data.copy.data;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.song.position.IVirtualPosition;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedPosition;
import log.charter.services.data.selection.SelectionManager;

public interface ICopyData {
	static <C extends IVirtualPosition, P extends C, T extends P, V extends CopiedPosition<T>> void simplePaste(
			final ChartData chartData, final SelectionManager selectionManager, final PositionType type,
			final FractionalPosition position, final List<V> positionsToPaste, final boolean convertFromBeats) {
		final ImmutableBeatsMap beats = chartData.beats();
		final List<T> positions = type.<C, P, T>manager().getItems(chartData);
		final List<T> positionsToSelect = new ArrayList<>(positionsToPaste.size());

		positionsToPaste.forEach(copiedPosition -> {
			try {
				final T value = copiedPosition.getValue(beats, position, convertFromBeats);
				if (value != null) {
					positions.add(value);
					positionsToSelect.add(value);
				}
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		});

		positions.sort(IVirtualConstantPosition.comparator(beats));
		selectionManager.addSelectionForPositions(type, positionsToSelect);
	}

	public boolean isEmpty();

	public void paste(ChartData chartData, SelectionManager selectionManager, FractionalPosition basePosition,
			boolean convertFromBeats);
}
