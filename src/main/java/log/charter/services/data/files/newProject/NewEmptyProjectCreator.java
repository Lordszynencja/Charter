package log.charter.services.data.files.newProject;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.PathsConfig;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.services.data.files.SongFileHandler;
import log.charter.sound.audioFormats.AudioFileMetadata;
import log.charter.sound.data.AudioData;
import log.charter.util.FileChooseUtils;

public class NewEmptyProjectCreator {
	private CharterFrame charterFrame;
	private NewProjectService newProjectService;
	private SongFileHandler songFileHandler;

	public void newProject() {
		if (!songFileHandler.askToSaveChanged()) {
			return;
		}

		final File songFile = FileChooseUtils.chooseMusicFile(charterFrame, PathsConfig.musicPath);
		if (songFile == null) {
			return;
		}

		final AudioFileMetadata metadata = AudioFileMetadata.readMetadata(songFile);

		final String defaultFolderName = newProjectService.generateFolderName(songFile, metadata);
		final File projectFolder = newProjectService.chooseSongFolder(songFile.getParent(), defaultFolderName);
		if (projectFolder == null) {
			return;
		}

		final AudioData musicData = AudioData.readFile(songFile);
		if (musicData == null) {
			showPopup(charterFrame, Label.MUSIC_DATA_NOT_FOUND);
			return;
		}

		final SongChart songChart = new SongChart(new BeatsMap(musicData.msLength()));
		newProjectService.fillMetadata(songChart, songFile, metadata);

		newProjectService.setDataForNewProject(projectFolder, songChart, musicData);
	}
}
