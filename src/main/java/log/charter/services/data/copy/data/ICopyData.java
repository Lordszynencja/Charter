package log.charter.services.data.copy.data;

import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.io.Logger;
import log.charter.services.data.copy.data.positions.CopiedFractionalPosition;
import log.charter.services.data.selection.SelectionManager;

public interface ICopyData {
	static <C extends IFractionalPosition, P extends C, T extends P, V extends CopiedFractionalPosition<T>> void simplePasteFractional(
			final ChartData chartData, final SelectionManager selectionManager, final PositionType type,
			final FractionalPosition basePosition, final List<V> positionsToPaste) {
		final ImmutableBeatsMap beats = chartData.beats();
		final List<T> positions = type.<C, P, T>manager().getItems(chartData);
		final List<T> positionsToSelect = new ArrayList<>(positionsToPaste.size());

		positionsToPaste.forEach(copiedPosition -> {
			try {
				final T newValue = copiedPosition.getValue(beats, basePosition);
				if (newValue == null) {
					return;
				}

				positionsToSelect.add(newValue);

				final Integer valueId = lastBeforeEqual(positions, newValue).findId();
				if (valueId == null) {
					positions.add(newValue);
					return;
				}

				final T value = positions.get(valueId);
				if (value.position().compareTo(newValue) == 0) {
					positions.set(valueId, newValue);
				} else {
					positions.add(valueId + 1, newValue);
				}
			} catch (final Exception e) {
				Logger.error("Couldn't paste position", e);
			}
		});

		positions.sort(IConstantFractionalPosition::compareTo);
		selectionManager.addSelectionForPositions(type, positionsToSelect);
	}

	public PositionType type();

	public boolean isEmpty();

	public void paste(ChartData chartData, SelectionManager selectionManager, FractionalPosition basePosition);
}
