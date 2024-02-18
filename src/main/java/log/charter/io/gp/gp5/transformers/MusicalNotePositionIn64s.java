package log.charter.io.gp.gp5.transformers;

import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class MusicalNotePositionIn64s {
	private static int gcd(final int a, final int b) {
		if (b > a) {
			return gcd(b, a);
		}
		if (a == b) {
			return a;
		}

		return gcd(a - b, b);
	}

	public final ArrayList2<Beat> beats;
	public final int beatId;
	public final int positionIn64s;
	public final int tupletProgress;
	public final int tupletNumerator;

	public MusicalNotePositionIn64s(final ArrayList2<Beat> beats, final int beatId) {
		this(beats, beatId, 0);
	}

	public MusicalNotePositionIn64s(final ArrayList2<Beat> beats, final int beatId, final int positionIn64s) {
		this(beats, beatId, positionIn64s, 0, 1);
	}

	public MusicalNotePositionIn64s(final ArrayList2<Beat> beats, final int beatId, final int positionIn64s,
			final int tupletProgress, final int tupletNumerator) {
		this.beats = beats;
		this.beatId = beatId;
		this.positionIn64s = positionIn64s;
		this.tupletProgress = tupletProgress;
		this.tupletNumerator = tupletProgress == 0 ? 1 : tupletNumerator;
	}

	private MusicalNotePositionIn64s move(final int offsetIn64s) {
		int newBeatId = beatId;
		int newPosition = positionIn64s + offsetIn64s;
		Beat beat = newBeatId < beats.size() ? beats.get(newBeatId) : beats.getLast();
		while (newPosition < 0) {
			newBeatId--;
			beat = newBeatId < beats.size() ? beats.get(newBeatId) : beats.getLast();
			newPosition += 64 / beat.noteDenominator;
		}
		while (newPosition >= 64 / beat.noteDenominator) {
			newBeatId++;
			beat = newBeatId < beats.size() ? beats.get(newBeatId) : beats.getLast();
			newPosition -= 64 / beat.noteDenominator;
		}

		return new MusicalNotePositionIn64s(beats, newBeatId, newPosition, tupletProgress, tupletNumerator);
	}

	private MusicalNotePositionIn64s move(final int offsetIn64s, final int tupletNumerator,
			final int tupletDenominator) {
		if (tupletNumerator == tupletDenominator) {
			return move(offsetIn64s);
		}

		int newBeatId = beatId;
		int newPosition = positionIn64s;

		final int gcd = gcd(tupletNumerator, this.tupletNumerator);
		final int numeratorMultiplier = tupletNumerator / gcd;
		int newTupletNumerator = this.tupletNumerator * numeratorMultiplier;
		int newTupletProgress = tupletProgress * numeratorMultiplier//
				+ offsetIn64s * tupletDenominator * this.tupletNumerator / gcd;

		while (newTupletProgress >= newTupletNumerator) {
			newPosition++;
			newTupletProgress -= newTupletNumerator;
		}

		if (newTupletProgress == 0) {
			newTupletNumerator = 1;
		}
		Beat beat = newBeatId < beats.size() ? beats.get(newBeatId) : beats.getLast();
		while (newPosition > 64 / beat.noteDenominator) {
			newBeatId++;
			beat = newBeatId < beats.size() ? beats.get(newBeatId) : beats.getLast();
			newPosition -= 64 / beat.noteDenominator;
		}

		return new MusicalNotePositionIn64s(beats, newBeatId, newPosition, newTupletProgress, newTupletNumerator);
	}

	public MusicalNotePositionIn64s move(final GPDuration duration) {
		return move(duration.length);
	}

	public MusicalNotePositionIn64s moveBackwards(final GPDuration duration) {
		return move(-duration.length);
	}

	public MusicalNotePositionIn64s move(final GPDuration duration, final int tupletNumerator,
			final int tupletDenominator) {
		return move(duration.length, tupletNumerator, tupletDenominator);
	}

	private double getPositionInBeat() {
		final Beat beat = beats.size() > beatId ? beats.get(beatId) : beats.getLast();
		double positionInBeat = positionIn64s * beat.noteDenominator / 64.0;
		positionInBeat += (double) tupletProgress * beat.noteDenominator / tupletNumerator / 64;
		return positionInBeat;
	}

	private int getPosition(final int beatPosition, final int length) {
		return beatPosition + (int) (getPositionInBeat() * length);
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
		return "MusicalNotePositionIn64s [positionInBeat=" + getPositionInBeat() + ", beatId=" + beatId
				+ ", positionIn64s=" + positionIn64s + ", tupletProgress=" + tupletProgress + ", tupletNumerator="
				+ tupletNumerator + ", position: " + getPosition() + "]";
	}

}