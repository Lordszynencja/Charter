package log.charter.song;

import static log.charter.song.enums.Position.findLastBefore;

import log.charter.util.CollectionUtils.ArrayList2;

public class OnBeat {
	public Beat beat;

	public OnBeat(final Beat beat) {
		this.beat = beat;
	}

	protected OnBeat(final ArrayList2<Beat> beats, final int position) {
		beat = findLastBefore(beats, position + 1);
	}

	public OnBeat(final ArrayList2<Beat> beats, final OnBeat other) {
		this(beats, other.beat.position);
	}
}
