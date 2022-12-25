package log.charter.song;

import static log.charter.io.rs.xml.song.SongArrangementXStreamHandler.readSong;
import static log.charter.io.rs.xml.vocals.VocalsXStreamHandler.readVocals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.config.Localization.Label;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rsc.xml.RocksmithChartProject;
import log.charter.song.notes.Note;
import log.charter.song.vocals.Vocals;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.RW;

public class SongChart {

	public static void addNote(final List<List<Note>> list, final Note n, final int diff) {
		if ((diff < 0) || (diff > 255)) {
			return;
		}

		while (list.size() < diff) {
			list.add(new ArrayList<>());
		}

		list.get(diff).add(n);
	}

	public String musicFileName;

	public String artistName;
	public String artistNameSort;
	public String title;
	public String albumName;
	public Integer albumYear;
	public BigDecimal crowdSpeed = new BigDecimal("1.0");

	public BeatsMap beatsMap;
	public ArrayList2<ArrangementChart> arrangements;
	public Vocals vocals = new Vocals();

	/**
	 * creates empty chart
	 */
	public SongChart(final int songLengthMs, final String musicFileName) {
		this.musicFileName = musicFileName;

		beatsMap = new BeatsMap(songLengthMs);
		arrangements = new ArrayList2<>();
	}

	/**
	 * creates chart from loaded project
	 */
	public SongChart(final int songLengthMs, final RocksmithChartProject project, final String dir) throws IOException {
		musicFileName = project.musicFileName;

		artistName = project.artistName;
		artistNameSort = project.artistNameSort;
		title = project.title;
		albumName = project.albumName;
		albumYear = project.albumYear;
		crowdSpeed = project.crowdSpeed;

		beatsMap = new BeatsMap(songLengthMs, project);

		arrangements = new ArrayList2<>();

		for (final String filename : project.arrangementFiles) {
			try {
				final String xml = RW.read(dir + filename);
				final SongArrangement songArrangement = readSong(xml);
				arrangements.add(new ArrangementChart(songArrangement));
			} catch (final Exception e) {
				throw new IOException(String.format(Label.MISSING_ARRANGEMENT_FILE.label(), filename));
			}
		}

		vocals = new Vocals(readVocals(RW.read(dir + "Vocals_RS2.xml")));
	}

	/**
	 * creates chart from rs xml
	 */
	public SongChart(final int songLengthMs, final String musicFileName, final SongArrangement songArrangement) {
		this.musicFileName = musicFileName;

		artistName = songArrangement.artistName;
		artistNameSort = songArrangement.artistNameSort;
		title = songArrangement.title;
		albumName = songArrangement.albumName;
		albumYear = songArrangement.albumYear;

		beatsMap = new BeatsMap(songLengthMs, songArrangement);
		arrangements = new ArrayList2<>();
		arrangements.add(new ArrangementChart(songArrangement));
	}
}
