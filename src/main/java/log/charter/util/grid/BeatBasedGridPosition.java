package log.charter.util.grid;

import log.charter.data.song.Beat;
import log.charter.util.collections.ArrayList2;

public class BeatBasedGridPosition extends GridPosition<Beat> {
	public BeatBasedGridPosition(final ArrayList2<Beat> beats, final int position) {
		super(beats, position);
	}
}
