package log.charter.io.rsc.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.song.Beat;
import log.charter.song.SongChart;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

@XStreamAlias("project")
public class RocksmithChartProject {
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

	public HashMap2<Integer, Integer> bookmarks = new HashMap2<>();

	public RocksmithChartProject() {
	}

	public RocksmithChartProject(final ModeManager modeManager, final ChartData data, final SongChart songChart) {
		editMode = modeManager.editMode;
		arrangement = data.currentArrangement;
		level = data.currentLevel;
		time = data.time;

		artistName = songChart.artistName;
		artistNameSort = songChart.artistNameSort;
		title = songChart.title;
		albumName = songChart.albumName;
		albumYear = songChart.albumYear;

		musicFileName = songChart.musicFileName;

		beats = songChart.beatsMap.beats;

		bookmarks = songChart.bookmarks;
	}
}
