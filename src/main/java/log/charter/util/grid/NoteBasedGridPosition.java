package log.charter.util.grid;

import log.charter.data.song.Beat;
import log.charter.util.collections.ArrayList2;

public class NoteBasedGridPosition extends GridPosition<Beat> {
	public NoteBasedGridPosition(final ArrayList2<Beat> beats, final int position) {
		super(beats, position);

		int lastMeasureBeatId = positionId;
		while (lastMeasureBeatId > 0 && !beats.get(lastMeasureBeatId).firstInMeasure) {
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
		final int startBeatId = positionId;
		final int additions = positions.get(positionId).noteDenominator;
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
		final int removals = positions.get(positionId > 0 && gridId == 0 ? positionId - 1 : positionId).noteDenominator;
		for (int i = 0; i < removals; i++) {
			super.previous();
			if (positionId != startBeatId && positions.get(positionId).firstInMeasure) {
				break;
			}
		}

		return this;
	}
}
