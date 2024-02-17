package log.charter.io.gp.gp5.transformers;

import static java.lang.Math.max;

import java.util.List;

import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.song.Beat;

public class MusicalNotePositionIn64s {
	public final List<Beat> beats;
	public final int beatId;
	public final int positionIn64s;
	public final int tupletProgress;
	public final int tupletDenominator;

	public MusicalNotePositionIn64s(final List<Beat> beats, final int beatId) {
		this(beats, beatId, 0);
	}

	public MusicalNotePositionIn64s(final List<Beat> beats, final int beatId, final int positionIn64s) {
		this(beats, beatId, positionIn64s, 0, 1);
	}

	public MusicalNotePositionIn64s(final List<Beat> beats, final int beatId, final int positionIn64s,
			final int tupletProgress, final int tupletDenominator) {
		this.beats = beats;
		this.beatId = beatId;
		this.positionIn64s = positionIn64s;
		this.tupletProgress = tupletProgress;
		this.tupletDenominator = tupletProgress == 0 ? 1 : tupletDenominator;
	}

	private MusicalNotePositionIn64s move(final int offsetIn64s) {
		return new MusicalNotePositionIn64s(beats, beatId, positionIn64s + offsetIn64s, tupletProgress,
				tupletDenominator);
	}

	public MusicalNotePositionIn64s move(final GPDuration duration) {
		return move(duration.length);
	}

	public MusicalNotePositionIn64s moveBackwards(final GPDuration duration) {
		return move(-duration.length);
	}

	public MusicalNotePositionIn64s move(final GPDuration duration, final int tupletNumerator,
			final int tupletDenominator) {
		if (tupletNumerator == tupletDenominator) {
			return move(duration.length);
		}

		return move(duration.length);// TODO add tuplets
	}

	public int getPosition() {// TODO add tuplets
		final Beat startingBeat = beats.get(beatId);
		int beatOffset64 = positionIn64s * startingBeat.noteDenominator;
		int beatOffset = beatOffset64 / 64;
		if (beatOffset64 < 0) {
			beatOffset64 += 64;
			beatOffset--;
		}
		if (beatId + beatOffset < 0) {
			return max(0, beats.get(0).position());
		}

		if (beatId + beatOffset >= beats.size()) {
			return beats.get(beats.size() - 1).position();
		}

		final Beat beatFrom = beats.get(beatId + beatOffset);
		if (beatId + beatOffset + 1 == beats.size()) {
			return beatFrom.position();
		}

		final Beat beatTo = beatOffset64 >= 0 ? beats.get(beatId + beatOffset + 1) : beats.get(beatId + beatOffset - 1);

		final int beatLength = beatTo.position() - beatFrom.position();
		final int positionInBeat = beatLength * (beatOffset64 % 64) / 64;

		return beatFrom.position() + positionInBeat;
	}
}