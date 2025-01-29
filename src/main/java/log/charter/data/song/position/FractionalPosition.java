package log.charter.data.song.position;

import static java.lang.Math.abs;
import static log.charter.util.CollectionUtils.closest;
import static log.charter.util.CollectionUtils.lastBeforeEqual;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.time.Position;
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

	private static Fraction generateFractionWithRounding(final double timeInBeat, final double beatLength) {
		final Fraction baseFraction = new Fraction((int) timeInBeat, (int) beatLength);
		final Fraction closestFraction = closest(fractionsToTryForRounding, baseFraction).find();

		final int positionFromClosest = closestFraction.multiply((int) beatLength).intValue();
		return abs(positionFromClosest - timeInBeat) <= 1 ? closestFraction : baseFraction;
	}

	private static FractionalPosition generateOnExistingBeat(final ImmutableBeatsMap beats, final int beatId,
			final double time, final boolean round) {
		final Beat beat = beats.get(beatId);
		final Beat nextBeat = beats.get(beatId + 1);
		final double timeInBeat = time - beat.position();
		final double beatLength = nextBeat.position() - beat.position();

		final Fraction fraction = generateFractionWithRounding(timeInBeat, beatLength);

		return new FractionalPosition(beatId, fraction);
	}

	private static FractionalPosition fromTime(final ImmutableBeatsMap beats, final double time, final boolean round) {
		Integer beatId = lastBeforeEqual(beats, new Position(time), IConstantPosition::compareTo).findId();
		if (beatId == null) {
			return new FractionalPosition();
		}

		if (beatId + 1 < beats.size()) {
			return generateOnExistingBeat(beats, beatId, time, round);
		}

		if (beats.size() < 2) {
			return new FractionalPosition();
		}

		final int usedBeatId = beats.size() - 2;
		final Beat beat = beats.get(usedBeatId);
		final Beat nextBeat = beats.get(usedBeatId + 1);
		final double beatLength = nextBeat.position() - beat.position();
		if (beatLength > 0) {
			beatId = (int) ((time - beat.position() + 1) / beatLength);
		}
		final double beatPosition = beat.position() + beatLength * (beatId - usedBeatId);
		final double timeInBeat = time - beatPosition;
		if (beatLength == 0) {
			return new FractionalPosition(beatId);
		}

		final Fraction fraction = round ? generateFractionWithRounding(timeInBeat, beatLength)
				: new Fraction((int) timeInBeat, (int) beatLength);

		return new FractionalPosition(beatId, fraction);
	}

	public static FractionalPosition fromTime(final ImmutableBeatsMap beats, final double time) {
		return fromTime(beats, time, false);
	}

	public static FractionalPosition fromTimeRounded(final ImmutableBeatsMap beats, final int time) {
		return fromTime(beats, time, true);
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

	public final int beatId;
	public final Fraction fraction;

	public FractionalPosition(final FractionalPosition other) {
		beatId = other.beatId;
		fraction = other.fraction;
	}

	public FractionalPosition(int beatId, Fraction fraction) {
		final int fullBeats = fraction.intValue();
		beatId = beatId + fullBeats;
		fraction = fraction.add(-fullBeats);
		if (fraction.negative()) {
			beatId--;
			fraction = fraction.add(1);
		}

		this.beatId = beatId;
		this.fraction = fraction;
	}

	public FractionalPosition(final int beatId) {
		this(beatId, new Fraction(0, 1));
	}

	public FractionalPosition(final Fraction fraction) {
		this(0, fraction);
	}

	public FractionalPosition() {
		this(0, new Fraction(0, 1));
	}

	public FractionalPosition negate() {
		return new FractionalPosition(-beatId, fraction.negate());
	}

	public FractionalPosition absolute() {
		return beatId < 0 ? negate() : this;
	}

	public FractionalPosition add(final int number) {
		return new FractionalPosition(beatId + number, fraction);
	}

	public FractionalPosition add(final Fraction fraction) {
		return new FractionalPosition(beatId, this.fraction.add(fraction));
	}

	public FractionalPosition add(final FractionalPosition other) {
		return new FractionalPosition(beatId + other.beatId, fraction.add(other.fraction));
	}

	public FractionalPosition multiply(final int number) {
		return new FractionalPosition(beatId * number, fraction.multiply(number));
	}

	public FractionalPosition multiply(final Fraction fraction) {
		return new FractionalPosition(this.fraction.multiply(fraction).add(fraction.multiply(beatId)));
	}

	public FractionalPosition round(final Fraction fraction) {
		if (fraction.numerator == 0) {
			return new FractionalPosition();
		}

		final FractionalPosition multiplied = multiply(new Fraction(fraction.denominator, fraction.numerator));
		final int rounded = (int) Math.round(multiplied.doubleValue());
		return new FractionalPosition(fraction.multiply(rounded));
	}

	public FractionalPosition floor() {
		return new FractionalPosition(beatId);
	}

	public FractionalPosition ceil() {
		return new FractionalPosition(fraction.numerator > 0 ? beatId + 1 : beatId);
	}

	private double getPosition(final double beatPosition, final double beatLength) {
		return beatPosition + fraction.doubleValue() * beatLength;
	}

	public double getPosition(final ImmutableBeatsMap beats) {
		if (beatId + 1 < beats.size()) {
			final Beat beat = beats.get(beatId);
			final Beat nextBeat = beats.get(beatId + 1);
			return getPosition(beat.position(), nextBeat.position() - beat.position());
		}

		final int usedBeatId = beats.size() - 2;
		final Beat beat = beats.get(usedBeatId);
		final Beat nextBeat = beats.get(usedBeatId + 1);
		final double beatLength = nextBeat.position() - beat.position();
		final double beatPosition = beat.position() + beatLength * (beatId - usedBeatId);
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

	public int compareTo(final Fraction o) {
		final int beatDifference = Integer.compare(beatId, 0);
		if (beatDifference != 0) {
			return beatDifference;
		}

		return fraction.compareTo(o);
	}

	@Override
	public FractionalPosition position() {
		return this;
	}

	public double doubleValue() {
		return beatId + fraction.doubleValue();
	}

	@Override
	public int hashCode() {
		return Objects.hash(beatId, fraction);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final FractionalPosition other = (FractionalPosition) obj;
		return beatId == other.beatId && Objects.equals(fraction, other.fraction);
	}

}