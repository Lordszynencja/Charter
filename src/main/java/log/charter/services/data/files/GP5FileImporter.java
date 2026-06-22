package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.io.gp.gp5.transformers.GP5BarOrderExtractor.getBarsOrder;
import static log.charter.io.gp.gp5.transformers.GP5FileTempoMapExtractor.getTempoMap;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.gui.panes.imports.ArrangementImportOptions;
import log.charter.io.Logger;
import log.charter.io.gp.GPFileImportOptions;
import log.charter.io.gp.GPFileImportOptionsPane;
import log.charter.io.gp.gp5.GP5FileReader;
import log.charter.io.gp.gp5.data.GP5File;
import log.charter.io.gp.gp5.transformers.GP5FileToSongChart;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.fixers.ArrangementFixer;

public class GP5FileImporter {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ChartTimeHandler chartTimeHandler;

	public void importGP5File(final File file) {
		try {
			final GPFileImportOptions importOptions = GPFileImportOptionsPane.getImportOptions(charterFrame, false);
			if (importOptions == null) {
				return;
			}

			final GP5File gp5File;
			try {
				gp5File = GP5FileReader.importGPFile(file, importOptions);
			} catch (final Exception e) {
				Logger.error("Couldn't import gp5 file " + file.getAbsolutePath(), e);
				showPopup(charterFrame, Label.COULDNT_IMPORT_GP5);

				return;
			}
			final List<Integer> barsOrder = getBarsOrder(gp5File.directions, gp5File.masterBars);

			final double startPosition = chartData.songChart.beatsMap.beats.get(0).position();
			final BeatsMap beatsMap;

			if (importOptions.importTempoMap) {
				beatsMap = getTempoMap(gp5File, startPosition, chartTimeHandler.maxTime(), barsOrder);
			} else {
				beatsMap = chartData.songChart.beatsMap;
			}

			final SongChart temporaryChart = GP5FileToSongChart.transform(gp5File, beatsMap, barsOrder, importOptions);

			final List<String> trackNames = gp5File.tracks.stream().map(t -> t.trackName).collect(Collectors.toList());
			new ArrangementImportOptions(charterFrame, arrangementFixer, charterMenuBar, chartData, temporaryChart,
					trackNames, importOptions);
		} catch (final Exception e) {
			Logger.error("Couldn't import gp5 file", e);
		}
	}
}
