package log.charter.data.song.position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;

public interface IVirtualPosition extends IVirtualConstantPosition {
	public static interface PositionDataTypeManager<C extends IVirtualConstantPosition, P extends C> {
		Comparator<C> comparator();

		List<C> constantOf(Stream<IVirtualConstantPosition> positions);
	}

	public static final PositionDataTypeManager<IConstantPosition, IPosition> positionManager = new PositionDataTypeManager<>() {
		@Override
		public Comparator<IConstantPosition> comparator() {
			return IConstantPosition::compareTo;
		}

		@Override
		public List<IConstantPosition> constantOf(final Stream<IVirtualConstantPosition> positions) {
			return positions//
					.map(p -> p.asConstantPosition())//
					.collect(Collectors.toList());
		}
	};

	public static final PositionDataTypeManager<IConstantFractionalPosition, IFractionalPosition> fractionalPositionManager = new PositionDataTypeManager<>() {
		@Override
		public Comparator<IConstantFractionalPosition> comparator() {
			return IConstantFractionalPosition::compareTo;
		}

		@Override
		public List<IConstantFractionalPosition> constantOf(final Stream<IVirtualConstantPosition> positions) {
			return positions//
					.map(p -> p.asConstantFraction())//
					.collect(Collectors.toList());
		}
	};

	@SuppressWarnings("unchecked")
	public static <T extends IVirtualPosition, U> List<U> listAsPositions(final List<T> list) {
		if (list.isEmpty()) {
			return new ArrayList<>();
		}

		final T element = list.get(0);
		if (element.isFraction()) {
			return (List<U>) list.stream().map(p -> p.asConstantFraction()).collect(Collectors.toList());
		}

		return (List<U>) list.stream().map(p -> p.asConstantPosition()).collect(Collectors.toList());
	}

	default IPosition asPosition() {
		return null;
	}

	default IFractionalPosition asFraction() {
		return null;
	}

	default void position(final ImmutableBeatsMap beats, final IVirtualConstantPosition newPosition) {
		if (isPosition()) {
			asPosition().position(newPosition.toPosition(beats).position());
		}
		if (isFraction()) {
			asFraction().fractionalPosition(newPosition.toFraction(beats).fractionalPosition());
		}
	}
}
