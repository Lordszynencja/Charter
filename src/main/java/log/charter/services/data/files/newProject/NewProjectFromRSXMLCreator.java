package log.charter.services.data.files.newProject;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.PathsConfig;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.LoadingDialog;
import log.charter.io.rs.xml.RSXMLToSongChart;
import log.charter.io.rs.xml.song.SongArrangement;
import log.charter.io.rs.xml.song.SongArrangementXStreamHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.sound.data.AudioData;
import log.charter.util.FileChooseUtils;

public class NewProjectFromRSXMLCreator {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private NewProjectService newProjectService;

	private File chooseSongFile() {
		final String fileChooseDir = modeManager.getMode() == EditMode.EMPTY ? PathsConfig.songsPath : chartData.path;
		return FileChooseUtils.chooseMusicFile(charterFrame, fileChooseDir);
	}

	private AudioData loadMusicFile(final File songFile) {
		final LoadingDialog loadingDialog = new LoadingDialog(charterFrame, 1);
		loadingDialog.setProgress(0, Label.LOADING_MUSIC_FILE.label());
		final AudioData musicData = AudioData.readFile(songFile);
		if (musicData == null) {
			loadingDialog.dispose();
			showPopup(charterFrame, Label.MUSIC_FILE_COULDNT_BE_LOADED);
			return null;
		}

		loadingDialog.dispose();
		return musicData;
	}

	private SongChart readSongArrangement(final File songFile) {
		final File arrangementFile = FileChooseUtils.chooseFile(charterFrame, songFile.getParent(),
				new String[] { ".xml" }, new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (arrangementFile == null) {
			return null;
		}

		final LoadingDialog loadingDialog = new LoadingDialog(charterFrame, 1);
		loadingDialog.setProgress(0, Label.LOADING_ARRANGEMENTS.label());
		final SongArrangement songArrangement = SongArrangementXStreamHandler.readSong(arrangementFile);
		final SongChart songChart = RSXMLToSongChart.makeSongChartForArrangement(songFile.getName(), songArrangement);
		loadingDialog.dispose();

		return songChart;
	}

	public void createSongWithImportFromArrangementXML() {
		final File songFile = chooseSongFile();
		if (songFile == null) {
			return;
		}

		final AudioData musicData = loadMusicFile(songFile);
		if (musicData == null) {
			return;
		}

		final SongChart songChart = readSongArrangement(songFile);
		if (songChart == null) {
			return;
		}

		newProjectService.setDataForNewProject(songFile.getParentFile(), songChart, musicData);
		modeManager.setArrangement(0);
		chartTimeHandler.nextTime(0);
	}
}
