package log.charter.io.gp.gp5;

import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.util.data.Fraction;

public class GP5FractionalPosition {
	private final ImmutableBeatsMap beats;
	private final int beatId;
	private final Fraction fraction;

	public GP5FractionalPosition(final ImmutableBeatsMap beats, final int beatId) {
		this(beats, beatId, new Fraction(0, 1));
	}

	public GP5FractionalPosition(final ImmutableBeatsMap beats, final int beatId, final Fraction fraction) {
		this.beats = beats;
		this.beatId = beatId;
		this.fraction = fraction;
	}

	private GP5FractionalPosition recalculateBeat(Fraction newFraction) {
		int newBeatId = beatId;
		Beat beat = beats.get(newBeatId);
		while (newFraction.numerator < 0) {
			newBeatId--;
			beat = beats.get(newBeatId);
			newFraction = newFraction.add(1, beat.noteDenominator);
		}
		while (newFraction.add(-1, beat.noteDenominator).numerator >= 0) {
			newBeatId++;
			beat = beats.get(newBeatId);
			newFraction = newFraction.add(-1, beat.noteDenominator);
		}

		return new GP5FractionalPosition(beats, newBeatId, newFraction);
	}

	public GP5FractionalPosition add(final Fraction fraction) {
		return recalculateBeat(this.fraction.add(fraction));
	}

	public GP5FractionalPosition move(final GPDuration duration) {
		return add(new Fraction(1, duration.denominator));
	}

	public GP5FractionalPosition moveBackwards(final GPDuration duration) {
		return add(new Fraction(-1, duration.denominator));
	}

	public GP5FractionalPosition move(final GPDuration duration, final int tupletNumerator, final int tupletDenominator,
			final int dots) {
		Fraction addFraction = new Fraction(1, duration.denominator);
		if (tupletDenominator != tupletNumerator) {
			addFraction = addFraction.multiply(tupletDenominator, tupletNumerator);
		}

		if (dots > 0) {
			int dotNumerator = 1;
			int dotDenominator = 1;
			for (int i = 0; i < dots; i++) {
				dotDenominator *= 2;
				dotNumerator += dotDenominator;
			}

			addFraction = addFraction.multiply(dotNumerator, dotDenominator);
		}

		return add(addFraction);
	}

	private int getPosition(final int beatPosition, final int beatLength, final int noteDenominator) {
		final double positionInBeat = (double) fraction.numerator / fraction.denominator * noteDenominator;
		return beatPosition + (int) (positionInBeat * beatLength);
	}

	public int getPosition() {
		if (beatId + 1 < beats.size()) {
			final Beat beat = beats.get(beatId);
			final Beat nextBeat = beats.get(beatId + 1);
			return getPosition(beat.position(), nextBeat.position() - beat.position(), beat.noteDenominator);
		}

		final int usedBeatId = beats.size() - 2;
		final Beat beat = beats.get(usedBeatId);
		final Beat nextBeat = beats.get(usedBeatId + 1);
		final int beatLength = nextBeat.position() - beat.position();
		final int beatPosition = beat.position() + beatLength * (beatId - usedBeatId);
		return getPosition(beatPosition, beatLength, nextBeat.noteDenominator);
	}

	@Override
	public String toString() {
		return beatId + " " + fraction;
	}
}