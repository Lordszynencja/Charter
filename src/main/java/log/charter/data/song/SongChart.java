package log.charter.data.song;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.song.vocals.Vocals;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.util.collections.ArrayList2;

public class SongChart {
	private static String cleanString(final String s) {
		return s == null ? "" : s.replace("\u0000", "");
	}

	public String musicFileName;

	private String artistName;
	private String artistNameSort;
	private String title;
	private String albumName;
	public Integer albumYear;

	public BeatsMap beatsMap;
	public Vocals vocals = new Vocals();
	public List<Arrangement> arrangements = new ArrayList<>();

	public Map<Integer, Integer> bookmarks = new HashMap<>();

	public SongChart() {
		beatsMap = new BeatsMap(1);
	}

	public SongChart(final BeatsMap beatsMap) {
		this.beatsMap = beatsMap;
	}

	public SongChart(final String musicFileName, final String artistName, final String artistNameSort,
			final String title, final String albumName, final Integer albumYear, final BeatsMap beatsMap,
			final ArrayList2<Arrangement> arrangements) {
		this.musicFileName = musicFileName;

		this.artistName(artistName);
		this.artistNameSort(artistNameSort);
		this.title(title);
		this.albumName(albumName);
		this.albumYear = albumYear;

		this.beatsMap = beatsMap;
		this.arrangements = arrangements;
	}

	/**
	 * creates chart from loaded project
	 */
	public SongChart(final ChartProject project, final String dir) throws IOException {
		musicFileName = project.musicFileName;

		artistName(project.artistName);
		artistNameSort(project.artistNameSort);
		title(project.title);
		albumName(project.albumName);
		albumYear = project.albumYear;

		beatsMap = new BeatsMap(project);

		if (project.vocals != null) {
			vocals = project.vocals;
		}
		if (project.arrangements != null) {
			arrangements = project.arrangements;
		}

		if (project.bookmarks != null) {
			bookmarks = project.bookmarks;
		}
	}

	public String artistName() {
		return artistName;
	}

	public void artistName(final String value) {
		artistName = cleanString(value);
	}

	public String artistNameSort() {
		return artistNameSort;
	}

	public void artistNameSort(final String value) {
		artistNameSort = cleanString(value);
	}

	public String title() {
		return title;
	}

	public void title(final String value) {
		title = cleanString(value);
	}

	public String albumName() {
		return albumName;
	}

	public void albumName(final String value) {
		albumName = cleanString(value);
	}

	public void moveBeats(final int chartLength, final int positionDifference) {
		for (final Beat beat : beatsMap.beats) {
			beat.position(max(0, min(chartLength, beat.position() + positionDifference)));
		}

		beatsMap.makeBeatsUntilSongEnd(chartLength);
	}

	public void moveContent(final int beatsToAdd) {
		vocals.vocals.forEach(v -> v.move(beatsToAdd));
		for (final Arrangement arrangement : arrangements) {
			arrangement.eventPoints.forEach(ep -> ep.move(beatsToAdd));
			arrangement.toneChanges.forEach(tc -> tc.move(beatsToAdd));

			for (final Level level : arrangement.levels) {
				level.anchors.forEach(tc -> tc.move(beatsToAdd));
				level.sounds.forEach(s -> s.move(beatsToAdd));
				level.handShapes.forEach(tc -> tc.move(beatsToAdd));
			}
		}
	}
}
