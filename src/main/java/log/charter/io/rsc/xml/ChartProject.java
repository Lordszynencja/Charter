package log.charter.io.rsc.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.ChartData;
import log.charter.data.managers.modes.EditMode;
import log.charter.song.Arrangement;
import log.charter.song.Beat;
import log.charter.song.SongChart;
import log.charter.song.vocals.Vocals;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

@XStreamAlias("project")
@XStreamInclude(value = { Vocals.class })
public class ChartProject {
	public int chartFormatVersion = 2;
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
	public ArrayList2<String> arrangementFiles = new ArrayList2<>();

	public ArrayList2<Beat> beats = new ArrayList2<>();
	public ArrayList2<Arrangement> arrangements = new ArrayList2<>();
	public Vocals vocals = new Vocals();

	public HashMap2<Integer, Integer> bookmarks = new HashMap2<>();

	public ChartProject() {
	}

	public ChartProject(final int time, final EditMode editMode, final ChartData data, final SongChart songChart) {
		this.editMode = editMode;
		arrangement = data.currentArrangement;
		level = data.currentLevel;
		this.time = time;

		artistName = songChart.artistName;
		artistNameSort = songChart.artistNameSort;
		title = songChart.title;
		albumName = songChart.albumName;
		albumYear = songChart.albumYear;

		musicFileName = songChart.musicFileName;

		beats = songChart.beatsMap.beats;
		arrangements = songChart.arrangements;
		vocals = songChart.vocals;

		bookmarks = songChart.bookmarks;
	}
}
