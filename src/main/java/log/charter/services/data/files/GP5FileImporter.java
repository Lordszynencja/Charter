package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;
import static log.charter.io.gp.gp5.transformers.GP5BarOrderExtractor.getBarsOrder;
import static log.charter.io.gp.gp5.transformers.GP5FileTempoMapExtractor.getTempoMap;

import java.io.File;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.gui.panes.imports.GP5ImportOptions;
import log.charter.io.Logger;
import log.charter.io.gp.gp5.GP5FileReader;
import log.charter.io.gp.gp5.data.GP5File;
import log.charter.io.gp.gp5.transformers.GP5FileToSongChart;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.song.BeatsMap;
import log.charter.song.SongChart;

public class GP5FileImporter {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ChartTimeHandler chartTimeHandler;

	private boolean askUserAboutUsingImportTempoMap() {
		return switch (askYesNo(charterFrame, Label.GP5_IMPORT_TEMPO_MAP, Label.USE_TEMPO_MAP_FROM_IMPORT)) {
			case YES -> true;
			default -> false;
		};
	}

	public void importGP5File(final File file) {
		final boolean useImportTempoMap = askUserAboutUsingImportTempoMap();

		try {
			final GP5File gp5File = GP5FileReader.importGPFile(file);
			final List<Integer> barsOrder = getBarsOrder(gp5File.directions, gp5File.masterBars);

			final int startPosition = chartData.songChart.beatsMap.beats.get(0).position();
			final BeatsMap beatsMap;
			if (useImportTempoMap) {
				beatsMap = getTempoMap(gp5File, startPosition, chartTimeHandler.maxTime(), barsOrder);
			} else {
				beatsMap = chartData.songChart.beatsMap;
			}

			final SongChart temporaryChart = GP5FileToSongChart.transform(gp5File, beatsMap, barsOrder);

			new GP5ImportOptions(charterFrame, arrangementFixer, charterMenuBar, chartData, temporaryChart);
		} catch (final Exception e) {
			Logger.error("Couldn't import gp5 file " + file.getAbsolutePath(), e);
			ComponentUtils.showPopup(charterFrame, Label.COULDNT_IMPORT_GP5);
		}
	}
}
