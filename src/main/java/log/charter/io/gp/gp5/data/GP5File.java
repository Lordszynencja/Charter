package log.charter.io.gp.gp5.data;

import java.util.List;
import java.util.Map;

public class GP5File {
	public final int version;
	public final ScoreInformation scoreInformation;
	public final int tempo;
	public final List<GPMasterBar> masterBars;
	public final List<GPTrackData> tracks;
	public final Map<Integer, List<GPBar>> trackBars;
	public final List<GPLyrics> lyrics;
	public final Directions directions;

	public GP5File(final int version, final ScoreInformation scoreInformation, final int tempo,
			final List<GPMasterBar> masterBars, final List<GPTrackData> tracks,
			final Map<Integer, List<GPBar>> trackBars, final List<GPLyrics> lyrics, final Directions directions) {
		this.version = version;
		this.scoreInformation = scoreInformation;
		this.tempo = tempo;
		this.masterBars = masterBars;
		this.tracks = tracks;
		this.trackBars = trackBars;
		this.lyrics = lyrics;
		this.directions = directions;
	}

	@Override
	public String toString() {
		return "GP5File [version=" + version + ", scoreInformation=" + scoreInformation + ", tempo=" + tempo
				+ ", masterBars=" + masterBars + ", tracks=" + tracks + ", trackBars=" + trackBars + ", lyrics="
				+ lyrics + "]";
	}

}
