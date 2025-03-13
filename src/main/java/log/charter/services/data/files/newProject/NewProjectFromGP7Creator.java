package log.charter.services.data.files.newProject;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.io.File;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.PathsConfig;
import log.charter.data.song.SongChart;
import log.charter.gui.CharterFrame;
import log.charter.io.Logger;
import log.charter.io.gp.gp7.GP7PlusFileImporter;
import log.charter.io.gp.gp7.GP7ZipReader;
import log.charter.io.gp.gp7.data.GP7Asset;
import log.charter.io.gp.gp7.data.GPIF;
import log.charter.services.data.files.SongFileHandler;
import log.charter.sound.audioFormats.AudioFileMetadata;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioData.DifferentChannelAmountException;
import log.charter.sound.data.AudioData.DifferentSampleRateException;
import log.charter.sound.data.AudioData.DifferentSampleSizesException;
import log.charter.sound.utils.AudioGenerator;
import log.charter.util.FileChooseUtils;
import log.charter.util.Utils;

public class NewProjectFromGP7Creator {
	private static class SongFileData {
		public final File file;
		public final boolean fromGPIF;

		public SongFileData(final File file, final boolean fromGPIF) {
			this.file = file;
			this.fromGPIF = fromGPIF;
		}

		public void deleteTempFile() {
			if (!fromGPIF || file == null) {
				return;
			}
			file.delete();
		}
	}

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

	private SongFileData getSongFile(final File gpFile, final GPIF gpif) {
		File songFile = getGPIFSongFile(gpFile, gpif);
		if (songFile != null) {
			return new SongFileData(songFile, true);
		}

		songFile = FileChooseUtils.chooseMusicFile(charterFrame, PathsConfig.musicPath);
		return new SongFileData(songFile, false);
	}

	private void addGPIFMetadata(final AudioFileMetadata metadata, final GPIF gpif) {
		if (metadata.artist.isBlank() && gpif.score.artist != null && !gpif.score.artist.isBlank()) {
			metadata.artist = gpif.score.artist;
		}
		if (metadata.title.isBlank() && gpif.score.title != null && !gpif.score.title.isBlank()) {
			metadata.title = gpif.score.title;
		}
		if (metadata.album.isBlank() && gpif.score.album != null && !gpif.score.album.isBlank()) {
			metadata.album = gpif.score.album;
		}
	}

	private AudioData getAudioData(final GPIF gpif, final SongFileData songFile) {
		AudioData musicData = AudioData.readFile(songFile.file);
		if (musicData == null) {
			showPopup(charterFrame, Label.MUSIC_DATA_NOT_FOUND);
			songFile.deleteTempFile();
			return null;
		}

		gpif.sampleRate = musicData.format.getSampleRate();

		if (songFile.fromGPIF && gpif.backingTrack.framePadding != null && gpif.backingTrack.framePadding > 0) {
			final double offset = gpif.backingTrack.framePadding / gpif.sampleRate;
			final AudioData silence = AudioGenerator.generateSilence(offset, musicData.format);
			try {
				musicData = silence.join(musicData);
			} catch (DifferentSampleSizesException | DifferentChannelAmountException | DifferentSampleRateException e) {
				Logger.error("Couldn't add silence to audio", e);
			}
		}

		return musicData;
	}

	private void createProjectForInternal(final File gpFile) {
		final GPIF gpif = gp7PlusFileImporter.importGPIF(gpFile);
		if (gpif == null) {
			return;
		}

		final SongFileData songFile = getSongFile(gpFile, gpif);
		if (songFile.file == null) {
			return;
		}

		final AudioFileMetadata metadata = AudioFileMetadata.readMetadata(songFile.file);
		addGPIFMetadata(metadata, gpif);

		final String defaultFolderName = newProjectService.generateFolderName(songFile.file, metadata);
		final File songFolder = newProjectService.chooseSongFolder(songFile.file.getParent(), defaultFolderName);
		if (songFolder == null) {
			songFile.deleteTempFile();
			return;
		}

		final AudioData musicData = getAudioData(gpif, songFile);
		if (musicData == null) {
			return;
		}

		final SongChart songChart = gp7PlusFileImporter.transformGPIFToSongChart(gpif, true);
		newProjectService.fillMetadata(songChart, songFile.file, metadata);
		newProjectService.setDataForNewProject(songFolder, songChart, musicData);
	}

}
