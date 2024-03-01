package log.charter.io.rs.xml;

import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.song.Arrangement;
import log.charter.song.BeatsMap;
import log.charter.song.SongChart;
import log.charter.util.CollectionUtils.ArrayList2;

public class RSXMLToSongChart {
	public static SongChart makeSongChartForArrangement(final String musicFileName,
			final SongArrangement songArrangement) {
		final BeatsMap beatsMap = new BeatsMap(songArrangement);
		final Arrangement arrangements = RSXMLToArrangement.toArrangement(songArrangement, beatsMap.beats);

		return new SongChart(musicFileName, songArrangement.artistName, songArrangement.artistNameSort,
				songArrangement.title, songArrangement.albumName, songArrangement.albumYear, beatsMap,
				new ArrayList2<>(arrangements));
	}
}
