package log.charter.data.song.position;

import log.charter.data.song.Beat;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.util.data.Fraction;

public class FractionalPosition implements Comparable<FractionalPosition> {
	private final ImmutableBeatsMap beats;
	private final int beatId;
	private final Fraction fraction;

	public FractionalPosition(final BeatsMap beats, final int beatId) {
		this(beats.immutable, beatId, new Fraction(0, 1));
	}

	public FractionalPosition(final ImmutableBeatsMap beats, final int beatId) {
		this(beats, beatId, new Fraction(0, 1));
	}

	public FractionalPosition(final BeatsMap beats, final int beatId, final Fraction fraction) {
		this(beats.immutable, beatId, fraction);
	}

	public FractionalPosition(final ImmutableBeatsMap beats, final int beatId, final Fraction fraction) {
		this.beats = beats;
		this.beatId = beatId;
		this.fraction = fraction;
	}

	private FractionalPosition recalculateBeat(Fraction newFraction) {
		final int fullBeats = newFraction.intValue();
		int newBeatId = beatId + fullBeats;
		newFraction = newFraction.add(-fullBeats);
		if (newFraction.numerator < 0) {
			newBeatId--;
			newFraction = newFraction.add(1);
		}

		return new FractionalPosition(beats, newBeatId, newFraction);
	}

	public FractionalPosition add(final Fraction fraction) {
		return recalculateBeat(this.fraction.add(fraction));
	}

	private int getPosition(final int beatPosition, final int beatLength) {
		return beatPosition + (int) (fraction.doubleValue() * beatLength);
	}

	public int getPosition() {
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

	@Override
	public int compareTo(final FractionalPosition o) {
		final int beatDifference = Integer.compare(beatId, o.beatId);
		if (beatDifference != 0) {
			return beatDifference;
		}

		return fraction.compareTo(o.fraction);
	}
}