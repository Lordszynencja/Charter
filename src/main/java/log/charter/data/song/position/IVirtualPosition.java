package log.charter.data.song.position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;

public interface IVirtualPosition extends IVirtualConstantPosition {

	public static interface PositionDataTypeManager<C extends IVirtualConstantPosition, P extends C> {
		Comparator<C> comparator();

		default List<C> constantOf(final Collection<IVirtualConstantPosition> positions) {
			return constantOf(positions.stream());
		}

		default List<P> of(final Collection<IVirtualPosition> positions) {
			return of(positions.stream());
		}

		List<C> constantOf(Stream<IVirtualConstantPosition> positions);

		List<P> of(Stream<IVirtualPosition> positions);
	}

	public static final PositionDataTypeManager<IConstantPosition, IPosition> positionManager = new PositionDataTypeManager<>() {
		@Override
		public Comparator<IConstantPosition> comparator() {
			return IConstantPosition::compareTo;
		}

		@Override
		public List<IConstantPosition> constantOf(final Stream<IVirtualConstantPosition> positions) {
			return positions//
					.flatMap(p -> p.asConstantPosition().stream())//
					.collect(Collectors.toList());
		}

		@Override
		public List<IPosition> of(final Stream<IVirtualPosition> positions) {
			return positions//
					.flatMap(p -> p.asPosition().stream())//
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
					.flatMap(p -> p.asConstantFraction().stream())//
					.collect(Collectors.toList());
		}

		@Override
		public List<IFractionalPosition> of(final Stream<IVirtualPosition> positions) {
			return positions//
					.flatMap(p -> p.asFraction().stream())//
					.collect(Collectors.toList());
		}
	};

	@SuppressWarnings("unchecked")
	public static <T extends IVirtualPosition, U> List<U> listAsPositions(final List<T> list) {
		if (list.isEmpty()) {
			return new ArrayList<>();
		}

		final T element = list.get(0);
		if (element.isFractionalPosition()) {
			return (List<U>) list.stream().flatMap(p -> p.asConstantFraction().stream()).collect(Collectors.toList());
		}

		return (List<U>) list.stream().flatMap(p -> p.asConstantPosition().stream()).collect(Collectors.toList());
	}

	default Optional<IPosition> asPosition() {
		return Optional.empty();
	}

	default Optional<IFractionalPosition> asFraction() {
		return Optional.empty();
	}

	default void position(final ImmutableBeatsMap beats, final IVirtualConstantPosition newPosition) {
		if (isPosition()) {
			asPosition().get().position(newPosition.positionAsPosition(beats).position());
		}
		if (isFractionalPosition()) {
			asFraction().get().fractionalPosition(newPosition.positionAsFraction(beats).fractionalPosition());
		}
	}
}
