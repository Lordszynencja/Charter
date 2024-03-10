package log.charter.util.grid;

import static java.lang.Math.floor;
import static log.charter.data.song.position.IConstantPosition.findLastIdBeforeEqual;

import log.charter.data.config.Config;
import log.charter.data.song.Beat;
import log.charter.data.song.position.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public class GridPosition<T extends IPosition> {

	public static GridPosition<Beat> create(final ArrayList2<Beat> beats, final int position) {
		switch (Config.gridType) {
		case NOTE:
			return new NoteBasedGridPosition(beats, position);
		case BEAT:
			return new BeatBasedGridPosition(beats, position);
		case MEASURE:
			return new MeasureBasedGridPosition(beats, position);
		default:
			return new GridPosition<>(beats, position);
		}
	}

	public final int gridSize = Config.gridSize;
	protected final ArrayList2<T> positions;

	public int positionId;
	public int gridId;

	public GridPosition(final ArrayList2<T> positions, final int position) {
		this.positions = positions;

		if (position <= positions.get(0).position()) {
			positionId = 0;
			return;
		}
		if (position >= positions.getLast().position()) {
			positionId = positions.size() - 1;
			return;
		}

		positionId = findLastIdBeforeEqual(positions, position);

		final int currentPosition = positions.get(positionId).position();
		final int nextPosition = positions.get(positionId + 1).position();
		final int beatLength = nextPosition - currentPosition;
		final int distanceInBeat = position - currentPosition;
		gridId = (int) floor(1.0 * (distanceInBeat + 1) * gridSize / beatLength);
	}

	public GridPosition<T> next() {
		gridId++;

		if (gridId >= Config.gridSize) {
			positionId++;
			gridId = 0;
		}

		if (positionId >= positions.size() - 1) {
			positionId = positions.size() - 1;
			gridId = 0;
		}

		return this;
	}

	public GridPosition<T> previous() {
		gridId--;

		if (gridId < 0) {
			positionId--;
			gridId = gridSize - 1;
		}

		if (positionId < 0) {
			positionId = 0;
			gridId = 0;
		}

		return this;
	}

	public int position() {
		final int beatPosition = positions.get(positionId).position();
		if (gridId == 0) {
			return beatPosition;
		}

		final int nextBeatPosition = positions.get(positionId + 1).position();
		return beatPosition + (nextBeatPosition - beatPosition) * gridId / gridSize;
	}
}
