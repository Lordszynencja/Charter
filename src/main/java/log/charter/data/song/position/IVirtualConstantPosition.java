package log.charter.data.song.position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;

public interface IVirtualConstantPosition {
	public static Comparator<IVirtualConstantPosition> comparator(final ImmutableBeatsMap beats) {
		return (a, b) -> compare(beats, a, b);
	}

	public static int compare(final ImmutableBeatsMap beats, final IVirtualConstantPosition a,
			final IVirtualConstantPosition b) {
		if (a.isFractionalPosition() && b.isFractionalPosition()) {
			return a.asConstantFraction().get().compareTo(b.asConstantFraction().get());
		}

		return a.positionAsPosition(beats).compareTo(b.positionAsPosition(beats));
	}

	public static IVirtualConstantPosition distance(final ImmutableBeatsMap beats, final IVirtualConstantPosition a,
			final IVirtualConstantPosition b) {
		if (a.isFractionalPosition() && b.isFractionalPosition()) {
			return a.asConstantFraction().get().distance(b.asConstantFraction().get());
		}

		return a.positionAsPosition(beats).distance(b.positionAsPosition(beats));
	}

	@SuppressWarnings("unchecked")
	public static <T extends IVirtualConstantPosition, U> List<U> listAsPositions(final List<T> list) {
		if (list.isEmpty()) {
			return new ArrayList<>();
		}

		final T element = list.get(0);
		if (element.isFractionalPosition()) {
			return (List<U>) list.stream().flatMap(p -> p.asConstantFraction().stream()).collect(Collectors.toList());
		}

		return (List<U>) list.stream().flatMap(p -> p.asConstantPosition().stream()).collect(Collectors.toList());
	}

	default Optional<IConstantPosition> asConstantPosition() {
		return Optional.empty();
	}

	default Optional<IConstantFractionalPosition> asConstantFraction() {
		return Optional.empty();
	}

	default boolean isPosition() {
		return asConstantPosition().isPresent();
	}

	default boolean isFractionalPosition() {
		return asConstantFraction().isPresent();
	}

	IConstantPosition positionAsPosition(ImmutableBeatsMap beats);

	IConstantFractionalPosition positionAsFraction(ImmutableBeatsMap beats);
}
