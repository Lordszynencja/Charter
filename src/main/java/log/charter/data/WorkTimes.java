package log.charter.data;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.Arrangement;
import log.charter.data.song.SongChart;
import log.charter.data.song.vocals.VocalPath;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;
import log.charter.io.workTimes.xml.ChartWorkTimesXStreamHandler;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.utils.Framer;
import log.charter.util.RW;
import log.charter.util.Utils;

public class WorkTimes implements Initiable {
	private static final String workTimesPath = Utils.defaultConfigDir + "workTimes.xml";

	@XStreamAlias("arrangementWorkTime")
	public static class ArrangementWorkTime {
		public int position;
		public String name;
		public int time = 0;
	}

	@XStreamAlias("chartWorkTimes")
	@XStreamInclude({ ArrangementWorkTime.class })
	public static class ChartWorkTimes {
		public String name = "";

		public int tempoWorkTime = 0;
		public int showlightsWorkTime = 0;
		public final Map<String, ArrangementWorkTime> arrangements = new HashMap<>();
	}

	private ChartData chartData;
	private CharterContext charterContext;
	private CharterFrame charterFrame;
	private ModeManager modeManager;

	private final Framer timeConsumedFramer = new Framer(this::addTime, 1);

	private int saveCounter = 0;
	private final Map<String, ChartWorkTimes> charts = new HashMap<>();

	@Override
	public void init() {
		load();

		timeConsumedFramer.skipFramesIfNeeded = false;
		timeConsumedFramer.start("work times");
		charterContext.addOnExit(this::stop);
	}

	public void stop() {
		timeConsumedFramer.stop();
		save();
	}

	private void load() {
		final File f = new File(workTimesPath);
		if (!f.exists()) {
			return;
		}

		try {
			final Map<String, ChartWorkTimes> loaded = ChartWorkTimesXStreamHandler.read(f);
			if (loaded != null) {
				charts.putAll(loaded);
			}
		} catch (final Exception e) {
			Logger.error("Couldn't load work times", e);
		}
	}

	private void save() {
		try {
			System.out.println("Saving");
			final String xml = ChartWorkTimesXStreamHandler.write(charts);
			RW.write(new File(workTimesPath), xml);
		} catch (final Exception e) {
			Logger.error("Couldn't save work times", e);
		}
	}

	private ChartWorkTimes getCurrentChartInfo() {
		final SongChart songChart = chartData.songChart;
		final String chartId = songChart.id;
		if (!charts.containsKey(chartId)) {
			charts.put(chartId, new ChartWorkTimes());
		}

		final ChartWorkTimes chartWorkTimes = charts.get(chartId);
		chartWorkTimes.name = songChart.artistName() + " - " + songChart.title();

		return chartWorkTimes;
	}

	private void updateVocalPathWorkTime(final ChartWorkTimes chartWorkTimes) {
		final VocalPath vocalPath = chartData.currentVocals();
		if (vocalPath.id == null) {
			vocalPath.id = Utils.generateRandomId(vocalPath.name);
		}

		if (!chartWorkTimes.arrangements.containsKey(vocalPath.id)) {
			chartWorkTimes.arrangements.put(vocalPath.id, new ArrangementWorkTime());
		}

		final ArrangementWorkTime arrangementWorkTime = chartWorkTimes.arrangements.get(vocalPath.id);
		arrangementWorkTime.time++;
		arrangementWorkTime.name = vocalPath.name;
	}

	private void updateArrangementWorkTime(final ChartWorkTimes chartWorkTimes) {
		final Arrangement arrangement = chartData.currentArrangement();
		if (arrangement.id == null) {
			arrangement.id = Utils.generateRandomId(arrangement.getTypeNameLabel());
		}

		if (!chartWorkTimes.arrangements.containsKey(arrangement.id)) {
			chartWorkTimes.arrangements.put(arrangement.id, new ArrangementWorkTime());
		}

		final ArrangementWorkTime arrangementWorkTime = chartWorkTimes.arrangements.get(arrangement.id);
		arrangementWorkTime.time++;
		arrangementWorkTime.name = arrangement.getTypeNameLabel();
	}

	private void updateWorkTime(final ChartWorkTimes chartWorkTimes) {
		switch (modeManager.getMode()) {
			case TEMPO_MAP:
				chartWorkTimes.tempoWorkTime++;
				break;
			case SHOWLIGHTS:
				chartWorkTimes.showlightsWorkTime++;
				break;
			case VOCALS:
				updateVocalPathWorkTime(chartWorkTimes);
				break;
			case GUITAR:
				updateArrangementWorkTime(chartWorkTimes);
				break;
			default:
				Logger.error("Work time on unknown mode! " + modeManager.getMode());
				break;
		}
	}

	private void updateNumbers(final ChartWorkTimes chartWorkTimes) {
		int deletedId = -1;
		final Set<String> unusedIds = new HashSet<>();
		for (final Entry<String, ArrangementWorkTime> arrangementWorkTime : chartWorkTimes.arrangements.entrySet()) {
			if (arrangementWorkTime.getValue().position < 0) {
				deletedId--;
			} else {
				unusedIds.add(arrangementWorkTime.getKey());
			}
		}

		int position = 1;
		for (final VocalPath vocalPath : chartData.songChart.vocalPaths) {
			unusedIds.remove(vocalPath.id);
			final ArrangementWorkTime arrangementWorkTime = chartWorkTimes.arrangements.get(vocalPath.id);
			if (arrangementWorkTime != null) {
				arrangementWorkTime.position = position++;
			}
		}

		position = 1;
		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			unusedIds.remove(arrangement.id);
			final ArrangementWorkTime arrangementWorkTime = chartWorkTimes.arrangements.get(arrangement.id);
			if (arrangementWorkTime != null) {
				arrangementWorkTime.position = position++;
			}
		}

		for (final String unusedId : unusedIds) {
			chartWorkTimes.arrangements.get(unusedId).position = deletedId--;
		}
	}

	public void addTime(final double dt) {
		if (charterFrame.isFocused() && !modeManager.modeIs(EditMode.EMPTY)) {
			System.out.println("Adding work time");
			final ChartWorkTimes currentChartInfo = getCurrentChartInfo();
			updateWorkTime(currentChartInfo);
			updateNumbers(currentChartInfo);
		}

		if (--saveCounter <= 0) {
			save();
			saveCounter = 1;
		}
	}

}
