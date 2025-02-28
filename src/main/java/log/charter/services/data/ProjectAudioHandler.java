package log.charter.services.data;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Stem;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.simple.LoadingDialog;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.io.Logger;
import log.charter.services.data.files.SongFilesBackuper;
import log.charter.sound.SoundFileType;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioData.DifferentChannelAmountException;
import log.charter.sound.data.AudioData.DifferentSampleRateException;
import log.charter.sound.data.AudioData.DifferentSampleSizesException;
import log.charter.sound.utils.AudioGenerator;
import log.charter.util.RW;

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

	private AudioData mainAudio = AudioGenerator.generateEmpty(0);
	private final List<AudioData> stems = new ArrayList<>();
	private int selectedStem = -1;

	public void importAudio(final File file) {
		final AudioData musicData = LoadingDialog.load(charterFrame, 1, dialog -> {
			dialog.setProgress(0, Label.LOADING_MUSIC_FILE);
			final AudioData result = AudioData.readFile(file);
			dialog.addProgress(Label.LOADING_DONE);

			return result;
		}, "Importing audio");

		if (musicData != null) {
			changeAudio(musicData);
		}
	}

	public void addStemOffset(final int stemId, final double offset) {
		final Stem stem = chartData.songChart.stems.get(stemId);
		stem.offset += offset;

		AudioData stemAudioData = stems.get(stemId);

		if (offset >= 0) {
			final AudioData silence = AudioGenerator.generateSilence(offset, stemAudioData.format.getSampleRate(),
					stemAudioData.format.getChannels(), stemAudioData.format.getSampleSizeInBits() / 8);
			try {
				stemAudioData = silence.join(stemAudioData);
			} catch (final DifferentSampleSizesException | DifferentChannelAmountException
					| DifferentSampleRateException e) {
				Logger.error("couldn't pan the stem", e);
			}
		} else {
			stemAudioData = stemAudioData.removeFromStart(-offset);
		}

		stems.set(stemId, stemAudioData);
	}

	public void readStems() {
		stems.clear();

		final List<Stem> songStems = chartData.songChart.stems;

		LoadingDialog.doWithLoadingDialog(charterFrame, songStems.size(), dialog -> {
			for (int i = 0; i < songStems.size(); i++) {
				dialog.setProgress(i, Label.LOADING_STEM.format(i));
				final Stem stem = songStems.get(i);
				AudioData stemAudioData = AudioData.readFile(new File(stem.getPath(chartData)));
				if (stemAudioData == null) {
					ComponentUtils.showPopup(charterFrame, Label.COULDNT_LOAD_AUDIO, stem.getPath(chartData));
					stemAudioData = AudioGenerator.generateEmpty(0);
				}

				stems.add(stemAudioData);
				addStemOffset(i, stem.offset);
			}
			dialog.addProgress(Label.LOADING_DONE);
		}, "Loading stems");
	}

	public void addStem(File file) {
		String path = file.getAbsolutePath();
		boolean local = false;
		if (path.startsWith(chartData.path)) {
			local = true;
			path = path.substring(chartData.path.length());
		} else {
			if (askYesNo(charterFrame, Label.COPY_AUDIO, Label.COPY_AUDIO_TO_PROJECT_FOLDER) == ConfirmAnswer.YES) {
				final File from = file;
				local = true;
				path = "stems/" + file.getName();
				file = new File(chartData.path, path);
				RW.copy(from, file);
			}
		}

		final File loadingFile = file;
		final AudioData stemAudioData = LoadingDialog.load(charterFrame, 1, dialog -> {
			dialog.setProgress(0, Label.LOADING_MUSIC_FILE);
			final AudioData result = AudioData.readFile(loadingFile);
			dialog.addProgress(Label.LOADING_DONE);

			return result;
		}, "Loading stem audio");
		if (stemAudioData == null) {
			ComponentUtils.showPopup(charterFrame, Label.COULDNT_LOAD_AUDIO, loadingFile.getAbsolutePath());
			return;
		}

		String stemName = ComponentUtils.askForInput(charterFrame, Label.AUDIO_STEM_NAME, "");
		while (stemName != null && stemName.isBlank()) {
			ComponentUtils.showPopup(charterFrame, Label.AUDIO_STEM_NAME_CANT_BE_EMPTY);
			stemName = ComponentUtils.askForInput(charterFrame, Label.AUDIO_STEM_NAME, "");
		}
		if (stemName == null) {
			return;
		}

		addStem(stemName, path, local, stemAudioData);
	}

	private void addStem(final String name, final String path, final boolean local, final AudioData audioData) {
		stems.add(audioData);
		chartData.songChart.stems.add(new Stem(name, path, local));
	}

	public void removeStem(final int stem) {
		if (stem < 0 || stem >= stems.size()) {
			return;
		}

		stems.remove(stem);
		chartData.songChart.stems.remove(stem);
	}

	public int getSelectedStem() {
		return selectedStem;
	}

	public void selectStem(final int stem) {
		if (stem < 0 || stem >= chartData.songChart.stems.size()) {
			selectedStem = -1;
		} else {
			selectedStem = stem;
		}
	}

	public void changeStemsOffset(final double offsetChange) {
		for (int i = 0; i < stems.size(); i++) {
			addStemOffset(i, offsetChange);
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
			defaultWrittenFileType().write(loadingDialog, mainAudio, file);
			chartData.songChart.musicFileName = file.getName();
		}, Label.SAVING_AUDIO.label());
	}

	public void changeAudio(final AudioData audio) {
		this.mainAudio = audio;
		waveFormDrawer.recalculateMap();
		saveAudio(true);
	}

	public void setAudio(final AudioData audio) {
		this.mainAudio = audio;
		waveFormDrawer.recalculateMap();
		saveAudio(false);
	}

	public AudioData getAudio() {
		if (selectedStem >= 0 && selectedStem < stems.size()) {
			return stems.get(selectedStem);
		}

		return mainAudio;
	}

	public double getVolume() {
		if (selectedStem >= 0 && selectedStem < stems.size()) {
			return chartData.songChart.stems.get(selectedStem).volume;
		}

		return Config.audio.volume;
	}

	public void setVolume(final double volume) {
		if (selectedStem >= 0 && selectedStem < stems.size()) {
			chartData.songChart.stems.get(selectedStem).volume = volume;
		} else {
			Config.audio.volume = volume;
			Config.markChanged();
		}
	}

	public double audioStemLength(final int stem) {
		return stem < 0 || stem >= stems.size() ? 0 : stems.get(stem).msLength();
	}

	public double audioLengthMs() {
		return mainAudio.msLength();
	}

}
