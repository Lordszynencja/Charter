package log.charter.services.data;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.simple.LoadingDialog;
import log.charter.services.data.files.SongFilesBackuper;
import log.charter.sound.SoundFileType;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioUtils;

public class ProjectAudioHandler {
	public static SoundFileType defaultWrittenFileType() {
		return !Config.baseAudioFormat.canBeWritten() ? SoundFileType.FLAC : Config.baseAudioFormat;
	}

	public static String defaultAudioFileName() {
		return "song." + defaultWrittenFileType().extension;
	}

	private ChartData chartData;
	private CharterFrame charterFrame;
	private WaveFormDrawer waveFormDrawer;

	private AudioData audio = AudioUtils.generateSilence(0, AudioUtils.DEF_RATE, 1, 1);

	public void importAudio(final File file) {
		final AudioData musicData = AudioData.readFile(file);
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

		LoadingDialog.doWithLoadingDialog(charterFrame, 1, loadingDialog -> {
			defaultWrittenFileType().write(loadingDialog, audio, file);
			chartData.songChart.musicFileName = file.getName();
		}, Label.SAVING_AUDIO.label());
	}

	public void changeAudio(final AudioData audio) {
		this.audio = audio;
		waveFormDrawer.recalculateMap();
		saveAudio(true);
	}

	public void setAudio(final AudioData audio) {
		this.audio = audio;
		waveFormDrawer.recalculateMap();
		saveAudio(false);
	}

	public AudioData getAudio() {
		return audio;
	}
}
