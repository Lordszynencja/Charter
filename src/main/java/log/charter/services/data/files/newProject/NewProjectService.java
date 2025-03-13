package log.charter.services.data.files.newProject;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.util.FileUtils.cleanFileName;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.PathsConfig;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.SongFolderSelectPane;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.data.files.SongFileHandler;
import log.charter.sound.audioFormats.AudioFileMetadata;
import log.charter.sound.data.AudioData;

public class NewProjectService {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private ProjectAudioHandler projectAudioHandler;
	private SongFileHandler songFileHandler;
	private TextTab textTab;

	public String generateFolderName(final File songFile, final AudioFileMetadata metadata) {
		String defaultFolderName;
		if (metadata.artist.isBlank() && metadata.title.isBlank()) {
			final String songFileName = songFile.getName();
			defaultFolderName = songFileName.substring(0, songFileName.lastIndexOf('.'));
		} else {
			defaultFolderName = "%s - %s".formatted(metadata.artist.isBlank() ? "unknown artist" : metadata.artist, //
					metadata.title.isBlank() ? "unknown title" : metadata.title);
		}
		defaultFolderName = cleanFileName(defaultFolderName);

		return defaultFolderName;
	}

	public File chooseSongFolder(final String audioFileDirectory, final String defaultFolderName) {
		File songFolder = null;

		while (songFolder == null) {
			final SongFolderSelectPane songFolderSelectPane = new SongFolderSelectPane(charterFrame,
					PathsConfig.songsPath, audioFileDirectory, defaultFolderName);

			if (songFolderSelectPane.isAudioFolderChosen()) {
				return new File(audioFileDirectory);
			}

			String folderName = songFolderSelectPane.getFolderName();
			if (folderName == null || folderName.isBlank()) {
				return null;
			}
			folderName = cleanFileName(folderName);

			songFolder = new File(PathsConfig.songsPath, folderName);

			if (songFolder.exists()) {
				songFolder = null;
				showPopup(charterFrame, Label.FOLDER_EXISTS_CHOOSE_DIFFERENT);
				continue;
			}
			if (!songFolder.mkdir()) {
				songFolder = null;
				showPopup(charterFrame, Label.COULDNT_CREATE_FOLDER_CHOOSE_DIFFERENT);
				continue;
			}
		}

		return songFolder;
	}

	public void fillMetadata(final SongChart songChart, final File songFile, final AudioFileMetadata metadata) {
		songChart.musicFileName = songFile.getName();
		songChart.artistName(metadata.artist);
		songChart.title(metadata.title);
		songChart.albumName(metadata.album);
		if (metadata.year != null) {
			songChart.albumYear = metadata.year;
		}
	}

	public void setDataForNewProject(final File projectFolder, final SongChart songChart, final AudioData musicData) {
		chartData.setNewSong(projectFolder, songChart, "project.rscp");
		textTab.setText("");

		projectAudioHandler.setAudio(musicData);
		projectAudioHandler.readStems();
		audioHandler.clear();

		chordTemplatesEditorTab.refreshTemplates();

		songFileHandler.save();
	}
}
