package log.charter.util.grid;

import java.util.List;

import log.charter.data.song.Beat;
import log.charter.data.song.position.FractionalPosition;

public class NoteBasedGridPosition extends GridPosition<Beat> {
	public NoteBasedGridPosition(final List<Beat> beats, final int position) {
		super(beats, position);

		snap();
	}

	public NoteBasedGridPosition(final List<Beat> beats, final FractionalPosition position) {
		super(beats, position);

		snap();
	}

	private void snap() {
		int lastMeasureBeatId = positionId;
		while (lastMeasureBeatId > 0 && !positions.get(lastMeasureBeatId).firstInMeasure) {
			lastMeasureBeatId--;
		}

		final int noteDenominator = positions.get(lastMeasureBeatId).noteDenominator;
		int measureGridId = (positionId - lastMeasureBeatId) * gridSize + gridId;
		measureGridId -= measureGridId % noteDenominator;
		positionId = lastMeasureBeatId + measureGridId / gridSize;
		gridId = measureGridId % gridSize;
	}

	@Override
	public void next() {
		final int startBeatId = positionId;
		final int additions = positions.get(positionId).noteDenominator;
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
		final int removals = positions.get(positionId > 0 && gridId == 0 ? positionId - 1 : positionId).noteDenominator;
		for (int i = 0; i < removals; i++) {
			super.previous();
			if (positionId != startBeatId && positions.get(positionId).firstInMeasure) {
				break;
			}
		}
	}
}
