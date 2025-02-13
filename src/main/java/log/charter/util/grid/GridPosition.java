package log.charter.util.grid;

import static java.lang.Math.floor;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.List;

import log.charter.data.config.Config;
import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.util.data.Fraction;

public class GridPosition<T extends Position> implements IVirtualConstantPosition {
	public static GridPosition<Beat> create(final List<Beat> beats, final double position) {
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
		if (position.isFraction()) {
			return create(beats, position.asConstantFraction().position());
		}

		return create(beats, position.asConstantPosition().position());
	}

	public final int gridSize = Config.gridSize;
	protected final List<T> positions;

	public int positionId;
	public int gridId;

	public GridPosition(final List<T> positions, final double position) {
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

		final double currentPosition = positions.get(positionId).position();
		final double nextPosition = positions.get(positionId + 1).position();
		final double beatLength = nextPosition - currentPosition;
		final double distanceInBeat = position - currentPosition;
		gridId = (int) floor(1.0 * (distanceInBeat + 1) * gridSize / beatLength);
	}

	public GridPosition(final List<T> positions, final FractionalPosition position) {
		this.positions = positions;
		positionId = position.beatId;
		gridId = (int) Math.floor(position.fraction.multiply(gridSize).doubleValue());
	}

	public void next() {
		gridId++;

		if (gridId >= Config.gridSize) {
			positionId++;
			gridId = 0;
		}

		if (positionId >= positions.size() - 1) {
			positionId = positions.size() - 1;
			gridId = 0;
		}
	}

	public void previous() {
		gridId--;

		if (gridId < 0) {
			positionId--;
			gridId = gridSize - 1;
		}

		if (positionId < 0) {
			positionId = 0;
			gridId = 0;
		}
	}

	public double position() {
		final double beatPosition = positions.get(positionId).position();
		if (gridId == 0) {
			return beatPosition;
		}

		final double nextBeatPosition = positions.get(positionId + 1).position();
		return beatPosition + (nextBeatPosition - beatPosition) * gridId / gridSize;
	}

	public FractionalPosition fractionalPosition() {
		return new FractionalPosition(positionId, new Fraction(gridId, gridSize));
	}

	@Override
	public IConstantPosition asConstantPosition() {
		return new ConstantPosition(position());
	}

	@Override
	public IConstantFractionalPosition asConstantFraction() {
		return fractionalPosition();
	}

	@Override
	public IConstantPosition toPosition(final ImmutableBeatsMap beats) {
		return asConstantPosition();
	}

	@Override
	public IConstantFractionalPosition toFraction(final ImmutableBeatsMap beats) {
		return asConstantFraction();
	}

	public int compareTo(final IVirtualConstantPosition position) {
		if (position.isPosition()) {
			return new ConstantPosition(position()).compareTo(position.asConstantPosition());
		}

		return fractionalPosition().compareTo(position.asConstantFraction());
	}

}
