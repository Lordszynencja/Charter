package log.charter.data.song.position.virtual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.time.IConstantPosition;

public interface IVirtualConstantPosition {
	public static Comparator<IVirtualConstantPosition> comparator(final ImmutableBeatsMap beats) {
		return (a, b) -> compare(beats, a, b);
	}

	public static int compare(final ImmutableBeatsMap beats, final IVirtualConstantPosition a,
			final IVirtualConstantPosition b) {
		if (a.isFraction() && b.isFraction()) {
			return a.asConstantFraction().compareTo(b.asConstantFraction());
		}

		return a.toPosition(beats).compareTo(b.toPosition(beats));
	}

	public static IVirtualConstantPosition distance(final ImmutableBeatsMap beats, final IVirtualConstantPosition a,
			final IVirtualConstantPosition b) {
		if (a.isFraction() && b.isFraction()) {
			return a.asConstantFraction().distance(b.asConstantFraction());
		}

		return a.toPosition(beats).distance(b.toPosition(beats));
	}

	public static IVirtualConstantPosition add(final ImmutableBeatsMap beats, final IVirtualConstantPosition a,
			final IVirtualConstantPosition b) {
		if (b.isPosition()) {
			final int positionA = a.toPosition(beats).position();
			final int positionB = b.toPosition(beats).position();
			return new ConstantPosition(positionA + positionB);
		}

		final FractionalPosition positionA = a.toFraction(beats).position();
		final FractionalPosition positionB = b.toFraction(beats).position();

		return positionA.add(positionB);
	}

	@SuppressWarnings("unchecked")
	public static <T extends IVirtualConstantPosition, U> List<U> listAsPositions(final List<T> list) {
		if (list.isEmpty()) {
			return new ArrayList<>();
		}

		final T element = list.get(0);
		if (element.isFraction()) {
			return (List<U>) list.stream().map(p -> p.asConstantFraction()).collect(Collectors.toList());
		}

		return (List<U>) list.stream().map(p -> p.asConstantPosition()).collect(Collectors.toList());
	}

	IConstantPosition asConstantPosition();

	IConstantFractionalPosition asConstantFraction();

	default boolean isPosition() {
		return asConstantPosition() != null;
	}

	default boolean isFraction() {
		return asConstantFraction() != null;
	}

	IConstantPosition toPosition(ImmutableBeatsMap beats);

	IConstantFractionalPosition toFraction(ImmutableBeatsMap beats);
}
