package log.charter.io.gp.gp7;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.io.gp.gp7.data.GP7Rhythm;
import log.charter.util.data.Fraction;

public class GP7FractionalPosition implements IConstantFractionalPosition {
	private final ImmutableBeatsMap beats;
	private final FractionalPosition position;

	public GP7FractionalPosition(final ImmutableBeatsMap beats, final FractionalPosition position) {
		this.beats = beats;
		this.position = position;
	}

	public GP7FractionalPosition(final ImmutableBeatsMap beats, final int beatId) {
		this(beats, new FractionalPosition(beatId, new Fraction(0, 1)));
	}

	public GP7FractionalPosition(final ImmutableBeatsMap beats, final int beatId, final Fraction fraction) {
		this(beats, new FractionalPosition(beatId, fraction));
	}

	private int getCurrentBeatDenominator() {
		return beats.get(position.beatId).noteDenominator;
	}

	private GP7FractionalPosition add(final Fraction fraction) {
		return new GP7FractionalPosition(beats, position.add(fraction));
	}

	public GP7FractionalPosition move(final GPDuration duration) {
		return add(new Fraction(getCurrentBeatDenominator(), duration.denominator));
	}

	public GP7FractionalPosition moveBackwards(final GPDuration duration) {
		return add(new Fraction(-getCurrentBeatDenominator(), duration.denominator));
	}

	public GP7FractionalPosition move(final GP7Rhythm rhythm) {
		Fraction addFraction = new Fraction(getCurrentBeatDenominator(), rhythm.duration.denominator);
		if (!rhythm.primaryTuplet.isSimple()) {
			addFraction = addFraction.multiply(rhythm.primaryTuplet.denominator, rhythm.primaryTuplet.numerator);

			if (!rhythm.secondaryTuplet.isSimple()) {
				addFraction = addFraction.multiply(rhythm.secondaryTuplet.denominator,
						rhythm.secondaryTuplet.numerator);
			}
		}

		if (rhythm.dots > 0) {
			int dotNumerator = 1;
			int dotDenominator = 1;
			for (int i = 0; i < rhythm.dots; i++) {
				dotDenominator *= 2;
				dotNumerator += dotDenominator;
			}

			addFraction = addFraction.multiply(dotNumerator, dotDenominator);
		}

		return add(addFraction);
	}

	@Override
	public FractionalPosition position() {
		return position;
	}
}