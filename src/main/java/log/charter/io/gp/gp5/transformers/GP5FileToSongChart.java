package log.charter.io.gp.gp5.transformers;

import static log.charter.io.gp.gp5.transformers.GP5ArrangementTransformer.makeArrangement;

import java.util.List;

import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.io.gp.gp5.data.GP5File;
import log.charter.io.gp.gp5.data.GPBar;
import log.charter.io.gp.gp5.data.GPTrackData;

public class GP5FileToSongChart {
	private static void addSongData(final GP5File gp5File, final SongChart chart) {
		chart.artistName(gp5File.scoreInformation.artist);
		chart.title(gp5File.scoreInformation.title);
		chart.albumName(gp5File.scoreInformation.album);
	}

	private static void addGP5Arrangements(final GP5File gp5File, final List<Integer> barsOrder,
			final SongChart chart) {
		for (final int trackId : gp5File.trackBars.keySet()) {
			final GPTrackData trackData = gp5File.tracks.get(trackId);
			if (trackData.isPercussion) {
				continue;
			}

			final List<GPBar> trackBars = gp5File.trackBars.get(trackId);
			final Arrangement arrangement = makeArrangement(chart.beatsMap, barsOrder, trackData, trackBars);
			chart.arrangements.add(arrangement);
		}
	}

	public static SongChart transform(final GP5File gp5File, final BeatsMap beatsMap, final List<Integer> barsOrder) {
		final SongChart chart = new SongChart(beatsMap);

		addSongData(gp5File, chart);
		addGP5Arrangements(gp5File, barsOrder, chart);

		return chart;
	}
}
