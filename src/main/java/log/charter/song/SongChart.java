package log.charter.song;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.io.rs.xml.song.SongArrangementXStreamHandler.readSong;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.readVocals;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import log.charter.data.config.Localization.Label;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.io.Logger;
import log.charter.io.rs.xml.RSXMLToArrangement;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.ChartProject;
import log.charter.song.notes.IPosition;
import log.charter.song.vocals.Vocals;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.RW;

public class SongChart {
	public String musicFileName;

	public String artistName;
	public String artistNameSort;
	public String title;
	public String albumName;
	public Integer albumYear;

	public BeatsMap beatsMap;
	public ArrayList2<Arrangement> arrangements = new ArrayList2<>();
	public Vocals vocals = new Vocals();

	public HashMap2<Integer, Integer> bookmarks = new HashMap2<>();

	/**
	 * creates empty chart for music file
	 */
	public SongChart(final int songLengthMs, final String musicFileName) {
		this.musicFileName = musicFileName;
		beatsMap = new BeatsMap(songLengthMs);
	}

	public SongChart(final BeatsMap beatsMap) {
		this.beatsMap = beatsMap;
	}

	public SongChart(final String musicFileName, final String artistName, final String artistNameSort,
			final String title, final String albumName, final Integer albumYear, final BeatsMap beatsMap,
			final ArrayList2<Arrangement> arrangements) {
		this.musicFileName = musicFileName;

		this.artistName = artistName;
		this.artistNameSort = artistNameSort;
		this.title = title;
		this.albumName = albumName;
		this.albumYear = albumYear;

		this.beatsMap = beatsMap;
		this.arrangements = arrangements;
	}

	/**
	 * creates chart from loaded project
	 */
	public SongChart(final int songLengthMs, final ChartProject project, final String dir) throws IOException {
		musicFileName = project.musicFileName;

		artistName = project.artistName;
		artistNameSort = project.artistNameSort;
		title = project.title;
		albumName = project.albumName;
		albumYear = project.albumYear;

		beatsMap = new BeatsMap(songLengthMs, project);

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

	public void moveEverything(final int audioLength, final int positionDifference) {
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
			positionToMove.position(min(audioLength, max(0, positionToMove.position() + positionDifference)));
		}

		beatsMap.makeBeatsUntilSongEnd(audioLength);
	}
}
