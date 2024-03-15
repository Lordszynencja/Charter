package log.charter.services.data.files;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;
import static log.charter.util.FileUtils.cleanFileName;

import java.io.File;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.SongFolderSelectPane;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioDataShort;
import log.charter.util.FileChooseUtils;

public class NewProjectCreator {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ProjectAudioHandler projectAudioHandler;
	private SongFileHandler songFileHandler;

	private File chooseSongFolder(final String audioFileDirectory, final String defaultFolderName) {
		File songFolder = null;

		while (songFolder == null) {
			final SongFolderSelectPane songFolderSelectPane = new SongFolderSelectPane(charterFrame, Config.songsPath,
					audioFileDirectory, defaultFolderName);
			songFolderSelectPane.setVisible(true);

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

		final Map<String, String> songData = MetaDataUtils.extractSongMetaData(songFile.getAbsolutePath());

		final String artist = songData.get("artist");
		final String title = songData.get("title");

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

		final AudioDataShort musicData = AudioDataShort.readFile(songFile);
		if (musicData == null) {
			showPopup(charterFrame, Label.MUSIC_DATA_NOT_FOUND);
			return;
		}

		final SongChart songChart = new SongChart(new BeatsMap(musicData.msLength()));
		songChart.artistName(songData.getOrDefault("artist", ""));
		songChart.title(songData.getOrDefault("title", ""));
		songChart.albumName(songData.getOrDefault("album", ""));
		try {
			songChart.albumYear = Integer.valueOf(songData.getOrDefault("year", ""));
		} catch (final NumberFormatException e) {
			songChart.albumYear = null;
		}

		chartData.setNewSong(songFolder, songChart, "project.rscp");
		projectAudioHandler.setAudio(musicData, !songFile.equals(new File(songFolder, "song.ogg")));
		songFileHandler.save();

		audioHandler.clear();
		audioHandler.setSong();
	}
}
