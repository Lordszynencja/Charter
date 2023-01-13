package log.charter.util.grid;

import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class MeasureBasedGridPosition extends GridPosition<Beat> {
	public MeasureBasedGridPosition(final ArrayList2<Beat> beats, final int position) {
		super(beats, position);

		int lastMeasureBeatId = positionId;
		while (!beats.get(lastMeasureBeatId).firstInMeasure) {
			lastMeasureBeatId--;
		}

		final int beatsInMeasure = beats.get(lastMeasureBeatId).beatsInMeasure;
		int measureGridId = (positionId - lastMeasureBeatId) * gridSize + gridId;
		measureGridId -= measureGridId % beatsInMeasure;
		positionId = lastMeasureBeatId + measureGridId / gridSize;
		gridId = measureGridId % gridSize;
	}

	@Override
	public GridPosition<Beat> next() {
		final int startBeatId = positionId;
		final int additions = positions.get(positionId).beatsInMeasure;
		for (int i = 0; i < additions; i++) {
			super.next();
			if (positionId != startBeatId && positions.get(positionId).firstInMeasure) {
				break;
			}
		}

		return this;
	}

	@Override
	public GridPosition<Beat> previous() {
		final int startBeatId = positionId;
		final int removals = positions.get(positionId > 0 && gridId == 0 ? positionId - 1 : positionId).beatsInMeasure;
		for (int i = 0; i < removals; i++) {
			super.previous();
			if (positionId != startBeatId && positions.get(positionId).firstInMeasure) {
				break;
			}
		}

		return this;
	}
}
