package log.charter.util.grid;

import java.util.List;

import log.charter.data.song.Beat;
import log.charter.data.song.position.FractionalPosition;

public class BeatBasedGridPosition extends GridPosition<Beat> {
	public BeatBasedGridPosition(final List<Beat> beats, final int position) {
		super(beats, position);
	}

	public BeatBasedGridPosition(final List<Beat> beats, final FractionalPosition position) {
		super(beats, position);
	}
}
