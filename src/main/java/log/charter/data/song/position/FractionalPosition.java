package log.charter.data.song.position;

import static java.lang.Math.abs;
import static log.charter.util.CollectionUtils.closest;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.io.xstream.converter.FractionalPositionConverter;
import log.charter.util.data.Fraction;

@XStreamConverter(FractionalPositionConverter.class)
public class FractionalPosition implements IConstantFractionalPosition {
	private static final List<Fraction> fractionsToTryForRounding = new ArrayList<>();

	private static void addFractions(final int numerator, final int denominator) {
		fractionsToTryForRounding.add(new Fraction(numerator, denominator));

		if (denominator <= 16) {
			addFractions(numerator * 2 - 1, denominator * 2);
			addFractions(numerator * 2 + 1, denominator * 2);
		}
	}

	static {
		for (final int denominator : new int[] { 2, 3, 5, 7, 9, 11, 13, 15 }) {
			for (int numerator = 1; numerator < denominator; numerator++) {
				addFractions(numerator, denominator);
			}
		}

		fractionsToTryForRounding.sort(Fraction::compareTo);
	}

	private static Fraction generateFractionWithRounding(final int timeInBeat, final int beatLength) {
		final Fraction baseFraction = new Fraction(timeInBeat, beatLength);
		final Fraction closestFraction = closest(fractionsToTryForRounding, baseFraction).find();

		final int positionFromClosest = closestFraction.multiply(beatLength).intValue();
		return abs(positionFromClosest - timeInBeat) <= 1 ? closestFraction : baseFraction;
	}

	private static FractionalPosition generateOnExistingBeat(final ImmutableBeatsMap beats, final int beatId,
			final int time, final boolean round) {
		final Beat beat = beats.get(beatId);
		final Beat nextBeat = beats.get(beatId + 1);
		final int timeInBeat = time - beat.position();
		final int beatLength = nextBeat.position() - beat.position();

		final Fraction fraction = generateFractionWithRounding(timeInBeat, beatLength);

		return new FractionalPosition(beatId, fraction);
	}

	public static FractionalPosition fromTime(final ImmutableBeatsMap beats, final int time, final boolean round) {
		final Integer beatId = lastBeforeEqual(beats, new Position(time), IConstantPosition::compareTo).findId();
		if (beatId == null) {
			return new FractionalPosition(0);
		}

		if (beatId + 1 < beats.size()) {
			return generateOnExistingBeat(beats, beatId, time, round);
		}

		final int usedBeatId = beats.size() - 2;
		final Beat beat = beats.get(usedBeatId);
		final Beat nextBeat = beats.get(usedBeatId + 1);
		final int beatLength = nextBeat.position() - beat.position();
		final int beatPosition = beat.position() + beatLength * (beatId - usedBeatId);
		final int timeInBeat = time - beatPosition;

		final Fraction fraction = round ? generateFractionWithRounding(timeInBeat, beatLength)
				: new Fraction(timeInBeat, beatLength);

		return new FractionalPosition(beatId, fraction);
	}

	public static FractionalPosition fromTime(final ImmutableBeatsMap beats, final int time) {
		return fromTime(beats, time, false);
	}

	public static FractionalPosition fromString(final String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		final int split = value.indexOf(' ');
		final int beatId = Integer.valueOf(value.substring(0, split));
		final Fraction fraction = Fraction.fromString(value.substring(split + 1));

		return new FractionalPosition(beatId, fraction);
	}

	private static FractionalPosition recalculateBeat(int newBeatId, Fraction newFraction) {
		final int fullBeats = newFraction.intValue();
		newBeatId = newBeatId + fullBeats;
		newFraction = newFraction.add(-fullBeats);
		if (newFraction.negative()) {
			newBeatId--;
			newFraction = newFraction.add(1);
		}

		return new FractionalPosition(newBeatId, newFraction);
	}

	public final int beatId;
	public final Fraction fraction;

	public FractionalPosition(final int beatId) {
		this(beatId, new Fraction(0, 1));
	}

	public FractionalPosition(final int beatId, final Fraction fraction) {
		this.beatId = beatId;
		this.fraction = fraction;
	}

	public FractionalPosition(final FractionalPosition other) {
		beatId = other.beatId;
		fraction = other.fraction;
	}

	private FractionalPosition recalculateBeat(final Fraction newFraction) {
		return recalculateBeat(beatId, newFraction);
	}

	public FractionalPosition negate() {
		return recalculateBeat(-beatId, fraction.negate());
	}

	public FractionalPosition absolute() {
		return beatId < 0 ? negate() : this;
	}

	public FractionalPosition add(final Fraction fraction) {
		return recalculateBeat(this.fraction.add(fraction));
	}

	public FractionalPosition add(final FractionalPosition other) {
		return recalculateBeat(beatId + other.beatId, fraction.add(other.fraction));
	}

	private int getPosition(final int beatPosition, final int beatLength) {
		return beatPosition + (int) (fraction.doubleValue() * beatLength);
	}

	public int getPosition(final ImmutableBeatsMap beats) {
		if (beatId + 1 < beats.size()) {
			final Beat beat = beats.get(beatId);
			final Beat nextBeat = beats.get(beatId + 1);
			return getPosition(beat.position(), nextBeat.position() - beat.position());
		}

		final int usedBeatId = beats.size() - 2;
		final Beat beat = beats.get(usedBeatId);
		final Beat nextBeat = beats.get(usedBeatId + 1);
		final int beatLength = nextBeat.position() - beat.position();
		final int beatPosition = beat.position() + beatLength * (beatId - usedBeatId);
		return getPosition(beatPosition, beatLength);
	}

	@Override
	public String toString() {
		return beatId + " " + fraction;
	}

	public String asString() {
		return "%d %s".formatted(beatId, fraction.asString());
	}

	public int compareTo(final FractionalPosition o) {
		final int beatDifference = Integer.compare(beatId, o.beatId);
		if (beatDifference != 0) {
			return beatDifference;
		}

		return fraction.compareTo(o.fraction);
	}

	@Override
	public FractionalPosition fractionalPosition() {
		return this;
	}

	public double doubleValue() {
		return beatId + fraction.doubleValue();
	}
}