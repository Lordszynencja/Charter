package log.charter.services.data;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.files.SongFilesBackuper;
import log.charter.sound.SoundFileType;
import log.charter.sound.StretchedFileLoader;
import log.charter.sound.data.AudioDataShort;

public class ProjectAudioHandler {
	public static SoundFileType defaultWrittenFileType() {
		return Config.baseAudioFormat.writer == null ? SoundFileType.WAV : Config.baseAudioFormat;
	}

	public static String defaultAudioFileName() {
		return "song." + defaultWrittenFileType().extension;
	}

	private AudioHandler audioHandler;
	private ChartData chartData;
	private WaveFormDrawer waveFormDrawer;

	private AudioDataShort audio = new AudioDataShort();

	public void importAudio(final File file) {
		final AudioDataShort musicData = AudioDataShort.readFile(file);
		if (musicData != null) {
			changeAudio(musicData);
		}
	}

	private void saveAudio(final boolean force) {
		if (!force && chartData.songChart.musicFileName != null
				&& chartData.songChart.musicFileName.equals(defaultAudioFileName())) {
			return;
		}

		final File file = new File(chartData.path, defaultAudioFileName());
		if (file.exists()) {
			SongFilesBackuper.makeAudioBackup(file);
		}

		defaultWrittenFileType().writer.accept(audio, file);
		chartData.songChart.musicFileName = file.getName();
	}

	public void changeAudio(final AudioDataShort audio) {
		this.audio = audio;

		StretchedFileLoader.removeGeneratedAndClear(chartData.path);
		waveFormDrawer.recalculateMap();
		saveAudio(true);

		audioHandler.audioChanged();
	}

	public void setAudio(final AudioDataShort audio) {
		this.audio = audio;

		waveFormDrawer.recalculateMap();
		saveAudio(false);

		audioHandler.audioChanged();
	}

	public AudioDataShort getAudio() {
		return audio;
	}
}
