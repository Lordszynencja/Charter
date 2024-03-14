package log.charter.io.rsc.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.SongChart;
import log.charter.data.song.vocals.Vocals;
import log.charter.services.editModes.EditMode;

@XStreamAlias("project")
@XStreamInclude({ Arrangement.class, Beat.class, Vocals.class })
public class ChartProject {
	public int chartFormatVersion = 3;
	public EditMode editMode = EditMode.TEMPO_MAP;
	public int arrangement = 0;
	public int level = 0;
	public int time = 0;

	public String artistName;
	public String artistNameSort;
	public String title;
	public String albumName;
	public Integer albumYear;

	public String musicFileName;
	public List<String> arrangementFiles = new ArrayList<>();

	public List<Beat> beats = new ArrayList<>();
	public List<Arrangement> arrangements = new ArrayList<>();
	public Vocals vocals = new Vocals();

	public Map<Integer, Integer> bookmarks = new HashMap<>();
	public String text;

	public ChartProject(final int time, final EditMode editMode, final ChartData data, final SongChart songChart,
			final String text) {
		this.editMode = editMode;
		arrangement = data.currentArrangement;
		level = data.currentLevel;
		this.time = time;

		artistName = songChart.artistName();
		artistNameSort = songChart.artistNameSort();
		title = songChart.title();
		albumName = songChart.albumName();
		albumYear = songChart.albumYear;

		musicFileName = songChart.musicFileName;

		beats = songChart.beatsMap.beats;
		arrangements = songChart.arrangements;
		vocals = songChart.vocals;

		bookmarks = songChart.bookmarks;
		this.text = text;
	}
}
