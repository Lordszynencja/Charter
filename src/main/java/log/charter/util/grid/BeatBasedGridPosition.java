package log.charter.util.grid;

import log.charter.song.Beat;
import log.charter.util.CollectionUtils.ArrayList2;

public class BeatBasedGridPosition extends GridPosition<Beat> {
	public BeatBasedGridPosition(final ArrayList2<Beat> beats, final int position) {
		super(beats, position);
	}
}
