package log.charter.util.grid;

import static java.lang.Math.floor;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.List;

import log.charter.data.config.Config;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.IConstantFractionalPosition;
import log.charter.data.song.position.IConstantPosition;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.song.position.Position;
import log.charter.util.data.Fraction;

public class GridPosition<T extends Position> implements IConstantPosition, IConstantFractionalPosition {
	public static GridPosition<Beat> create(final List<Beat> beats, final int position) {
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

	public static GridPosition<Beat> create(final List<Beat> beats, final FractionalPosition position) {
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

	public static GridPosition<Beat> create(final List<Beat> beats, final IVirtualConstantPosition position) {
		if (position.isFractionalPosition()) {
			return create(beats, position.asConstantFraction().get());
		}

		return create(beats, position.asConstantPosition().get().position());
	}

	public final int gridSize = Config.gridSize;
	protected final List<T> positions;

	public int positionId;
	public int gridId;

	public GridPosition(final List<T> positions, final int position) {
		this.positions = positions;
		if (positions.isEmpty() || position <= positions.get(0).position()) {
			positionId = 0;
			return;
		}
		if (position >= positions.get(positions.size() - 1).position()) {
			positionId = positions.size() - 1;
			return;
		}

		positionId = lastBeforeEqual(positions, new Position(position), IConstantPosition::compareTo).findId(0);

		final int currentPosition = positions.get(positionId).position();
		final int nextPosition = positions.get(positionId + 1).position();
		final int beatLength = nextPosition - currentPosition;
		final int distanceInBeat = position - currentPosition;
		gridId = (int) floor(1.0 * (distanceInBeat + 1) * gridSize / beatLength);
	}

	public GridPosition(final List<T> positions, final FractionalPosition position) {
		this.positions = positions;
		positionId = position.beatId;
		gridId = (int) Math.round(position.fraction.multiply(gridSize).doubleValue());
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

	@Override
	public int position() {
		final int beatPosition = positions.get(positionId).position();
		if (gridId == 0) {
			return beatPosition;
		}

		final int nextBeatPosition = positions.get(positionId + 1).position();
		return beatPosition + (nextBeatPosition - beatPosition) * gridId / gridSize;
	}

	@Override
	public FractionalPosition fractionalPosition() {
		return new FractionalPosition(positionId, new Fraction(gridId, gridSize));
	}

	@Override
	public IConstantPosition positionAsPosition(final ImmutableBeatsMap beats) {
		return this;
	}

	@Override
	public IConstantFractionalPosition positionAsFraction(final ImmutableBeatsMap beats) {
		return this;
	}
}
