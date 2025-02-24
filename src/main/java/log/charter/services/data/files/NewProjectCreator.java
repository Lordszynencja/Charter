package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.util.FileUtils.cleanFileName;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.SongFolderSelectPane;
import log.charter.gui.components.tabs.TextTab;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.audioFormats.AudioFileMetadata;
import log.charter.sound.data.AudioData;
import log.charter.util.FileChooseUtils;

public class NewProjectCreator {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private ProjectAudioHandler projectAudioHandler;
	private SongFileHandler songFileHandler;
	private TextTab textTab;

	private File chooseSongFolder(final String audioFileDirectory, final String defaultFolderName) {
		File songFolder = null;

		while (songFolder == null) {
			final SongFolderSelectPane songFolderSelectPane = new SongFolderSelectPane(charterFrame, Config.songsPath,
					audioFileDirectory, defaultFolderName);

			if (songFolderSelectPane.isAudioFolderChosen()) {
				return new File(audioFileDirectory);
			}

			String folderName = songFolderSelectPane.getFolderName();
			if (folderName == null || folderName.isBlank()) {
				return null;
			}
			folderName = cleanFileName(folderName);

			songFolder = new File(Config.songsPath, folderName);

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

	public void newSong() {
		if (!songFileHandler.askToSaveChanged()) {
			return;
		}

		final File songFile = FileChooseUtils.chooseMusicFile(charterFrame, Config.musicPath);
		if (songFile == null) {
			return;
		}

		final AudioFileMetadata metadata = AudioFileMetadata.readMetadata(songFile);

		final String artist = metadata.artist;
		final String title = metadata.title;

		String defaultFolderName;
		if (artist.isBlank() && title.isBlank()) {
			final String songFileName = songFile.getName();
			defaultFolderName = songFileName.substring(0, songFileName.lastIndexOf('.'));
		} else {
			defaultFolderName = "%s - %s".formatted(artist.isBlank() ? "unknown artist" : artist, //
					title.isBlank() ? "unknown title" : title);
		}
		defaultFolderName = cleanFileName(defaultFolderName);

		final File songFolder = chooseSongFolder(songFile.getParent(), defaultFolderName);
		if (songFolder == null) {
			return;
		}

		final AudioData musicData = AudioData.readFile(songFile);
		if (musicData == null) {
			showPopup(charterFrame, Label.MUSIC_DATA_NOT_FOUND);
			return;
		}

		final SongChart songChart = new SongChart(new BeatsMap(musicData.msLength()));
		songChart.musicFileName = songFile.getName();
		songChart.artistName(metadata.artist);
		songChart.title(metadata.title);
		songChart.albumName(metadata.album);
		if (metadata.year != null) {
			songChart.albumYear = metadata.year;
		}

		chartData.setNewSong(songFolder, songChart, "project.rscp");
		textTab.setText("");
		projectAudioHandler.setAudio(musicData);
		projectAudioHandler.readStems();
		songFileHandler.save();

		audioHandler.clear();
		chordTemplatesEditorTab.refreshTemplates();
	}
}
