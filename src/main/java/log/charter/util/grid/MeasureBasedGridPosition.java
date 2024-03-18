package log.charter.util.grid;

import java.util.List;

import log.charter.data.song.Beat;
import log.charter.data.song.position.FractionalPosition;

public class MeasureBasedGridPosition extends GridPosition<Beat> {
	public MeasureBasedGridPosition(final List<Beat> beats, final int position) {
		super(beats, position);

		snap();
	}

	public MeasureBasedGridPosition(final List<Beat> beats, final FractionalPosition position) {
		super(beats, position);

		snap();
	}

	private void snap() {
		int lastMeasureBeatId = positionId;
		while (!positions.get(lastMeasureBeatId).firstInMeasure) {
			lastMeasureBeatId--;
		}

		final int beatsInMeasure = positions.get(lastMeasureBeatId).beatsInMeasure;
		int measureGridId = (positionId - lastMeasureBeatId) * gridSize + gridId;
		measureGridId -= measureGridId % beatsInMeasure;
		positionId = lastMeasureBeatId + measureGridId / gridSize;
		gridId = measureGridId % gridSize;
	}

	@Override
	public void next() {
		final int startBeatId = positionId;
		final int additions = positions.get(positionId).beatsInMeasure;
		for (int i = 0; i < additions; i++) {
			super.next();
			if (positionId != startBeatId && positions.get(positionId).firstInMeasure) {
				break;
			}
		}
	}

	@Override
	public void previous() {
		final int startBeatId = positionId;
		final int removals = positions.get(positionId > 0 && gridId == 0 ? positionId - 1 : positionId).beatsInMeasure;
		for (int i = 0; i < removals; i++) {
			super.previous();
			if (positionId != startBeatId && positions.get(positionId).firstInMeasure) {
				break;
			}
		}
	}
}
