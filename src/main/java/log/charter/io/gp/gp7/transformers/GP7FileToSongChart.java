package log.charter.io.gp.gp7.transformers;

import java.util.List;

import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.io.gp.gp7.data.GPIF;

public class GP7FileToSongChart {
	private static void setMetadata(final SongChart chart, final GPIF gpif) {
		if (gpif.score.title != null && !gpif.score.title.isBlank()) {
			chart.title(gpif.score.title);
		}

		if (gpif.score.artist != null && !gpif.score.artist.isBlank()) {
			chart.artistName(gpif.score.artist);
		}

		if (gpif.score.album != null && !gpif.score.album.isBlank()) {
			chart.albumName(gpif.score.album);
		}
	}

	private static boolean emptyTrack(final Track track) {
		for (final GP7NotesWithPosition notes : track.notes) {
			if (!notes.notes.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public static SongChart transform(final GPIF gpif, final BeatsMap beatsMap) {
		final SongChart chart = new SongChart(beatsMap);
		setMetadata(chart, gpif);
		final List<Track> tracks = new NotesPerTrackReader(beatsMap.immutable, gpif).getTracks();

		for (final Track track : tracks) {
			if (emptyTrack(track)) {
				continue;
			}

			chart.arrangements.add(GP7TrackToArrangementTransformer.transform(beatsMap.immutable, track));
		}

		return chart;
	}
}
