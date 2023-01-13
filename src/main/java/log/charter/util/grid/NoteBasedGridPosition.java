package log.charter.util.grid;

import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class NoteBasedGridPosition extends GridPosition<Beat> {
	public NoteBasedGridPosition(final ArrayList2<Beat> beats, final int position) {
		super(beats, position);

		int lastMeasureBeatId = positionId;
		while (!beats.get(lastMeasureBeatId).firstInMeasure) {
			lastMeasureBeatId--;
		}

		final int noteDenominator = beats.get(lastMeasureBeatId).noteDenominator;
		int measureGridId = (positionId - lastMeasureBeatId) * gridSize + gridId;
		measureGridId -= measureGridId % noteDenominator;
		positionId = lastMeasureBeatId + measureGridId / gridSize;
		gridId = measureGridId % gridSize;
	}

	@Override
	public GridPosition<Beat> next() {
		final int additions = positions.get(positionId).noteDenominator;
		for (int i = 0; i < additions; i++) {
			super.next();
		}

		return this;
	}

	@Override
	public GridPosition<Beat> previous() {
		final int removals = positions.get(positionId > 0 && gridId == 0 ? positionId - 1 : positionId).noteDenominator;
		for (int i = 0; i < removals; i++) {
			super.previous();
		}

		return this;
	}
}
