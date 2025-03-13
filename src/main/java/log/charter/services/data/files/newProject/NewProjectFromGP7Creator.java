package log.charter.services.data.files.newProject;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.PathsConfig;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.io.gp.gp7.GP7PlusFileImporter;
import log.charter.io.gp.gp7.GP7ZipReader;
import log.charter.io.gp.gp7.data.GP7Asset;
import log.charter.io.gp.gp7.data.GPIF;
import log.charter.services.data.files.SongFileHandler;
import log.charter.sound.audioFormats.AudioFileMetadata;
import log.charter.sound.data.AudioData;
import log.charter.util.FileChooseUtils;
import log.charter.util.Utils;

public class NewProjectFromGP7Creator {

	private CharterFrame charterFrame;
	private GP7PlusFileImporter gp7PlusFileImporter;
	private NewProjectService newProjectService;
	private SongFileHandler songFileHandler;

	private File chooseGPFile() {
		return FileChooseUtils.chooseFile(charterFrame, PathsConfig.gpFilesPath, new String[] { ".gp" },
				Label.GP7_FILE.label());
	}

	public void createProject() {
		if (!songFileHandler.askToSaveChanged()) {
			return;
		}

		final File gpifFile = chooseGPFile();
		if (gpifFile == null) {
			return;
		}

		createProjectForInternal(gpifFile);
	}

	public void createProjectFor(final File gpFile) {
		if (!songFileHandler.askToSaveChanged()) {
			return;
		}

		createProjectForInternal(gpFile);
	}

	private File createTempSongFile(final File gpFile, final String zipPath) {
		int lastSlash = zipPath.lastIndexOf('/');
		if (lastSlash == -1) {
			lastSlash = zipPath.lastIndexOf('\\');
		}
		final String fileName = lastSlash == -1 ? zipPath : zipPath.substring(lastSlash + 1);
		final File tmpFile = new File(Utils.defaultConfigDir, fileName);
		GP7ZipReader.unpackFile(gpFile, zipPath, tmpFile);
		return tmpFile;
	}

	private File getGPIFSongFile(final File gpFile, final GPIF gpif) {
		if (gpif.backingTrack == null || gpif.backingTrack.source == null
				|| !gpif.backingTrack.source.equalsIgnoreCase("local") || gpif.backingTrack.assetId == null) {
			return null;
		}

		for (final GP7Asset asset : gpif.assets) {
			if (asset.id == gpif.backingTrack.assetId) {
				return createTempSongFile(gpFile, asset.embeddedFilePath);
			}
		}

		return null;
	}

	private void createProjectForInternal(final File gpFile) {
		final GPIF gpif = gp7PlusFileImporter.importGPIF(gpFile);
		if (gpif == null) {
			return;
		}

		File songFile = getGPIFSongFile(gpFile, gpif);
		if (songFile == null) {
			songFile = FileChooseUtils.chooseMusicFile(charterFrame, PathsConfig.musicPath);
			if (songFile == null) {
				return;
			}
		} else {
			songFile.deleteOnExit();
		}

		final AudioFileMetadata metadata = AudioFileMetadata.readMetadata(songFile);
		if (metadata.artist.isBlank() && gpif.score.artist != null && !gpif.score.artist.isBlank()) {
			metadata.artist = gpif.score.artist;
		}
		if (metadata.title.isBlank() && gpif.score.title != null && !gpif.score.title.isBlank()) {
			metadata.title = gpif.score.title;
		}
		if (metadata.album.isBlank() && gpif.score.album != null && !gpif.score.album.isBlank()) {
			metadata.album = gpif.score.album;
		}

		final String defaultFolderName = newProjectService.generateFolderName(songFile, metadata);
		final File songFolder = newProjectService.chooseSongFolder(songFile.getParent(), defaultFolderName);
		if (songFolder == null) {
			songFile.delete();
			return;
		}

		final AudioData musicData = AudioData.readFile(songFile);
		if (musicData == null) {
			showPopup(charterFrame, Label.MUSIC_DATA_NOT_FOUND);
			songFile.delete();
			return;
		}

		final SongChart songChart = gp7PlusFileImporter.transformGPIFToSongChart(gpif, true);
		newProjectService.fillMetadata(songChart, songFile, metadata);
		newProjectService.setDataForNewProject(songFolder, songChart, musicData);
	}

}
