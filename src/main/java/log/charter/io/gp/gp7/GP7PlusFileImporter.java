package log.charter.io.gp.gp7;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;
import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.gui.panes.imports.ArrangementImportOptions;
import log.charter.io.Logger;
import log.charter.io.gp.gp7.data.GP7Asset;
import log.charter.io.gp.gp7.data.GPIF;
import log.charter.io.gp.gp7.transformers.GP7FileToSongChart;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.util.Utils;

public class GP7PlusFileImporter {
	private static File createTempSongFile(final File gpFile, final String zipPath) {
		int lastSlash = zipPath.lastIndexOf('/');
		if (lastSlash == -1) {
			lastSlash = zipPath.lastIndexOf('\\');
		}

		final String fileName = lastSlash == -1 ? zipPath : zipPath.substring(lastSlash + 1);
		final File tmpFile = new File(Utils.defaultConfigDir, fileName);
		GP7ZipReader.unpackFile(gpFile, zipPath, tmpFile);
		tmpFile.deleteOnExit();

		return tmpFile;
	}

	public static File getGPIFSongFile(final File gpFile, final GPIF gpif) {
		if (!gpif.containsAudioTrackAsset()) {
			return null;
		}

		for (final GP7Asset asset : gpif.assets) {
			if (asset.id == gpif.backingTrack.assetId) {
				return createTempSongFile(gpFile, asset.embeddedFilePath);
			}
		}

		return null;
	}

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private GP7TempoReader gp7TempoReader;

	public GPIF importGPIF(final File file) {
		try {
			return GP7FileXStreamHandler.readGPIF(file);
		} catch (final Exception e) {
			Logger.error("Couldn't import GP7/8 file", e);
			showPopup(charterFrame, Label.COULDNT_IMPORT_GP7);
		}

		return null;
	}

	public void importGP7PlusFile(final File file) {
		final GPIF gpif = importGPIF(file);
		if (gpif == null) {
			return;
		}

		final boolean importBeatMap = askYesNo(charterFrame, Label.GP_IMPORT_TEMPO_MAP,
				Label.USE_TEMPO_MAP_FROM_IMPORT) == ConfirmAnswer.YES;

		final SongChart temporaryChart = transformGPIFToSongChart(gpif, importBeatMap);

		final List<String> trackNames = gpif.tracks.stream().map(t -> t.name).collect(Collectors.toList());
		new ArrangementImportOptions(charterFrame, arrangementFixer, charterMenuBar, chartData, temporaryChart,
				trackNames);
	}

	public SongChart transformGPIFToSongChart(final GPIF gpif, final boolean importBeatMap) {
		final BeatsMap beatsMap = gp7TempoReader.getTempoMap(gpif, importBeatMap);
		return GP7FileToSongChart.transform(gpif, beatsMap);
	}
}
