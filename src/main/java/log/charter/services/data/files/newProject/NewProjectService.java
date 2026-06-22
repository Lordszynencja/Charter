package log.charter.services.data.files.newProject;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.services.data.files.SongFileHandler.defaultProjectFileName;
import static log.charter.util.FileUtils.cleanFileName;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.SongFolderSelectPane;
import log.charter.gui.components.containers.SongFolderSelectPane.FolderSelectedType;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.data.files.SongFileHandler;
import log.charter.sound.audioFormats.AudioFileMetadata;
import log.charter.sound.data.AudioData;

public class NewProjectService {
	public static String generateDefaultChartFolderName(final String audioFileName, final String artist,
			final String title) {
		String defaultFolderName;
		if (artist.isBlank() && title.isBlank()) {
			final String songFileName = audioFileName;
			defaultFolderName = songFileName.substring(0, songFileName.lastIndexOf('.'));
		} else {
			defaultFolderName = "%s - %s".formatted(artist.isBlank() ? "unknown artist" : artist, //
					title.isBlank() ? "unknown title" : title);
		}
		defaultFolderName = cleanFileName(defaultFolderName);

		return defaultFolderName;
	}

	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private ProjectAudioHandler projectAudioHandler;
	private SongFileHandler songFileHandler;
	private TextTab textTab;

	public String generateFolderName(final File songFile, final AudioFileMetadata metadata) {
		return generateDefaultChartFolderName(songFile.getName(), metadata.artist, metadata.title);
	}

	public File chooseSongFolder(final File audioFileDirectory, final String defaultFolderName) {
		return chooseSongFolder(audioFileDirectory, defaultFolderName, null);
	}

	public File chooseSongFolder(final File audioFileDirectory, final String defaultFolderName,
			final File xmlFileDirectory) {
		File folder = null;

		while (folder == null) {
			final FolderSelectedType defaultType = xmlFileDirectory != null ? FolderSelectedType.XML_FILE
					: FolderSelectedType.CHARTS_DIR;
			final SongFolderSelectPane songFolderSelectPane = new SongFolderSelectPane(charterFrame, audioFileDirectory,
					defaultFolderName, null, xmlFileDirectory, defaultType);

			final FolderSelectedType folderType = songFolderSelectPane.getFolderType();
			if (folderType == null) {
				return null;
			}

			folder = songFolderSelectPane.getFolder();

			if (folderType == FolderSelectedType.CHARTS_DIR) {
				if (folder.exists()) {
					final ConfirmAnswer answer = ComponentUtils.askYesNo(charterFrame, Label.FOLDER_EXISTS,
							Label.FOLDER_EXISTS_MSG);
					if (answer == ConfirmAnswer.YES) {
						break;
					}
				} else if (!folder.mkdir()) {
					folder = null;
					showPopup(charterFrame, Label.COULDNT_CREATE_FOLDER_CHOOSE_DIFFERENT);
					continue;
				}
			}
		}

		return folder;
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
		chartData.setNewSong(projectFolder, songChart, defaultProjectFileName);
		textTab.setText("");

		projectAudioHandler.setAudio(musicData);
		projectAudioHandler.readStems();
		audioHandler.clear();

		chordTemplatesEditorTab.refreshTemplates();

		songFileHandler.save();
	}
}
