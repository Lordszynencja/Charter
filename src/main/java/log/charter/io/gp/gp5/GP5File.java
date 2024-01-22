package log.charter.io.gp.gp5;

import java.util.List;
import java.util.Map;

public class GP5File {
	public final int version;
	public final ScoreInformation scoreInformation;
	public final int tempo;
	public final List<GPMasterBar> masterBars;
	public final List<GPTrackData> tracks;
	public final Map<Integer, List<GPBar>> bars;
	public final List<GPLyrics> lyrics;

	public GP5File(final int version, final ScoreInformation scoreInformation, final int tempo,
			final List<GPMasterBar> masterBars, final List<GPTrackData> tracks, final Map<Integer, List<GPBar>> bars,
			final List<GPLyrics> lyrics) {
		this.version = version;
		this.scoreInformation = scoreInformation;
		this.tempo = tempo;
		this.masterBars = masterBars;
		this.tracks = tracks;
		this.bars = bars;
		this.lyrics = lyrics;
	}

	@Override
	public String toString() {
		return "GP5File [version=" + version + ", scoreInformation=" + scoreInformation + ", tempo=" + tempo
				+ ", masterBars=" + masterBars + ", tracks=" + tracks + ", bars=" + bars + ", lyrics=" + lyrics + "]";
	}

}
