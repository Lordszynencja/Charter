package log.charter.io.gp.gp5;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.util.data.Fraction;

public class GP5FractionalPosition implements IConstantFractionalPosition {
	private final ImmutableBeatsMap beats;
	private final FractionalPosition position;

	public GP5FractionalPosition(final ImmutableBeatsMap beats, final FractionalPosition position) {
		this.beats = beats;
		this.position = position;
	}

	public GP5FractionalPosition(final ImmutableBeatsMap beats, final int beatId) {
		this(beats, new FractionalPosition(beatId, new Fraction(0, 1)));
	}

	public GP5FractionalPosition(final ImmutableBeatsMap beats, final int beatId, final Fraction fraction) {
		this(beats, new FractionalPosition(beatId, fraction));
	}

	private int getCurrentBeatDenominator() {
		return beats.get(position.beatId).noteDenominator;
	}

	private GP5FractionalPosition add(final Fraction fraction) {
		return new GP5FractionalPosition(beats, position.add(fraction));
	}

	public GP5FractionalPosition move(final GPDuration duration) {
		return add(new Fraction(getCurrentBeatDenominator(), duration.denominator));
	}

	public GP5FractionalPosition moveBackwards(final GPDuration duration) {
		return add(new Fraction(-getCurrentBeatDenominator(), duration.denominator));
	}

	public GP5FractionalPosition move(final GPDuration duration, final int tupletNumerator, final int tupletDenominator,
			final int dots) {
		Fraction addFraction = new Fraction(getCurrentBeatDenominator(), duration.denominator);
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

	public int position() {
		return position(beats);
	}

	@Override
	public FractionalPosition fractionalPosition() {
		return position;
	}
}