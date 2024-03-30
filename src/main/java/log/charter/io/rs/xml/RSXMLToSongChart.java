package log.charter.io.rs.xml;

import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.util.collections.ArrayList2;

public class RSXMLToSongChart {
	public static SongChart makeSongChartForArrangement(final String musicFileName,
			final SongArrangement songArrangement) {
		final BeatsMap beatsMap = new BeatsMap(songArrangement);
		final Arrangement arrangements = RSXMLToArrangement.toArrangement(songArrangement, beatsMap.immutable);

		return new SongChart(musicFileName, songArrangement.artistName, songArrangement.artistNameSort,
				songArrangement.title, songArrangement.albumName, songArrangement.albumYear, beatsMap,
				new ArrayList2<>(arrangements));
	}
}
