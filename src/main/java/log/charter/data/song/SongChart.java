package log.charter.data.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.io.rs.xml.song.SongArrangementXStreamHandler.readSong;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.readVocals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import log.charter.data.config.Localization.Label;
import log.charter.data.song.position.IPosition;
import log.charter.data.song.vocals.Vocals;
import log.charter.io.Logger;
import log.charter.io.rs.xml.RSXMLToArrangement;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.services.data.files.SongFileHandler;
import log.charter.util.RW;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashMap2;

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
	public ArrayList2<Arrangement> arrangements = new ArrayList2<>();
	public Vocals vocals = new Vocals();

	public HashMap2<Integer, Integer> bookmarks = new HashMap2<>();

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

		if (project.arrangements != null) {
			arrangements = project.arrangements;
		}

		for (final String filename : project.arrangementFiles) {
			try {
				final String xml = RW.read(dir + filename);
				final SongArrangement songArrangement = readSong(xml);
				arrangements.add(RSXMLToArrangement.toArrangement(songArrangement, beatsMap.beats));
			} catch (final Exception e) {
				Logger.error("Couldn't load arrangement file " + filename, e);
				throw new IOException(String.format(Label.MISSING_ARRANGEMENT_FILE.label(), filename));
			}
		}
		project.arrangementFiles.clear();

		if (project.vocals != null) {
			vocals = project.vocals;
		} else {
			vocals = new Vocals(readVocals(RW.read(dir + SongFileHandler.vocalsFileName)));
		}

		bookmarks = project.bookmarks;
		if (bookmarks == null) {
			bookmarks = new HashMap2<>();
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

	public void moveEverything(final int chartLength, final int positionDifference) {
		final List<IPosition> positionsToMove = new LinkedList<>();
		positionsToMove.addAll(beatsMap.beats);
		for (final Arrangement arrangement : arrangements) {
			positionsToMove.addAll(arrangement.eventPoints);
			positionsToMove.addAll(arrangement.toneChanges);
			for (final Level level : arrangement.levels) {
				positionsToMove.addAll(level.anchors);
				positionsToMove.addAll(level.sounds);
				positionsToMove.addAll(level.handShapes);
			}
		}
		positionsToMove.addAll(vocals.vocals);

		for (final IPosition positionToMove : positionsToMove) {
			positionToMove.position(max(0, min(chartLength, positionToMove.position() + positionDifference)));
		}

		beatsMap.makeBeatsUntilSongEnd(chartLength);
	}
}
