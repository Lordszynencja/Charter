package log.charter.data.song;

import static log.charter.util.Utils.nvl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.data.song.vocals.VocalPath;
import log.charter.io.rsc.xml.ChartProject;

public class SongChart {
	private static String cleanString(final String s) {
		return s == null ? "" : s.replace("\u0000", "");
	}

	public String musicFileName;
	public List<Stem> stems = new ArrayList<>();

	private String artistName;
	private String artistNameSort;
	private String title;
	private String albumName;
	public Integer albumYear;

	public BeatsMap beatsMap;
	public List<VocalPath> vocalPaths = new ArrayList<>();
	public List<Arrangement> arrangements = new ArrayList<>();

	public Map<Integer, Double> bookmarks = new HashMap<>();

	public SongChart() {
		beatsMap = new BeatsMap(1);
	}

	public SongChart(final BeatsMap beatsMap) {
		this.beatsMap = beatsMap;
	}

	public SongChart(final String musicFileName, final String artistName, final String artistNameSort,
			final String title, final String albumName, final Integer albumYear, final BeatsMap beatsMap,
			final List<Arrangement> arrangements) {
		this.musicFileName = musicFileName;

		this.artistName(artistName);
		this.artistNameSort(artistNameSort);
		this.title(title);
		this.albumName(albumName);
		this.albumYear = albumYear;

		this.beatsMap = beatsMap;
		this.arrangements.addAll(arrangements);
	}

	/**
	 * creates chart from loaded project
	 */
	public SongChart(final ChartProject project, final String dir) throws IOException {
		musicFileName = project.musicFileName;

		stems = nvl(project.stems, new ArrayList<>());

		artistName(project.artistName);
		artistNameSort(project.artistNameSort);
		title(project.title);
		albumName(project.albumName);
		albumYear = project.albumYear;

		beatsMap = new BeatsMap(project);

		if (project.vocalPaths != null) {
			vocalPaths = project.vocalPaths;
		}
		if (project.vocals != null) {
			vocalPaths.add(project.vocals);
			project.vocals = null;
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

	private List<IFractionalPosition> getAllFractionalPositionContent() {
		final List<IFractionalPosition> positions = new ArrayList<>(10000);

		for (final VocalPath vocalPath : vocalPaths) {
			positions.addAll(vocalPath.vocals);
		}
		for (final Arrangement arrangement : arrangements) {
			positions.addAll(arrangement.eventPoints);
			positions.addAll(arrangement.toneChanges);

			for (final Level level : arrangement.levels) {
				positions.addAll(level.fhps);
				positions.addAll(level.sounds);
				positions.addAll(level.handShapes);
			}
		}

		return positions;
	}

	public void moveContent(final FractionalPosition from, final FractionalPosition offset) {
		getAllFractionalPositionContent().forEach(p -> {
			if (p.compareTo(from) >= 0) {
				p.move(offset);
			}
		});
	}

	public void moveContent(final int beatsToAdd) {
		getAllFractionalPositionContent().forEach(p -> p.move(beatsToAdd));
	}

	private void remove(final List<? extends IFractionalPosition> positions, final FractionalPosition from,
			final FractionalPosition to) {
		positions.removeIf(p -> p.compareTo(from) >= 0 && p.compareTo(to) < 0);
	}

	public void removeContent(final FractionalPosition from, final FractionalPosition to) {
		for (final VocalPath vocalPath : vocalPaths) {
			remove(vocalPath.vocals, from, to);
		}
		for (final Arrangement arrangement : arrangements) {
			remove(arrangement.eventPoints, from, to);
			remove(arrangement.toneChanges, from, to);

			for (final Level level : arrangement.levels) {
				remove(level.fhps, from, to);
				remove(level.sounds, from, to);
				remove(level.handShapes, from, to);
			}
		}
	}
}
