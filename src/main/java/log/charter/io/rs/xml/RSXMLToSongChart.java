package log.charter.io.rs.xml;

import static java.util.Arrays.asList;

import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.io.rs.xml.song.SongArrangement;

public class RSXMLToSongChart {
	public static SongChart makeSongChartForArrangement(final String musicFileName,
			final SongArrangement songArrangement) {
		final BeatsMap beatsMap = new BeatsMap(songArrangement);
		final Arrangement arrangement = RSXMLToArrangement.toArrangement(songArrangement, beatsMap.immutable);

		return new SongChart(musicFileName, songArrangement.artistName, songArrangement.artistNameSort,
				songArrangement.title, songArrangement.albumName, songArrangement.albumYear, beatsMap,
				asList(arrangement));
	}
}
