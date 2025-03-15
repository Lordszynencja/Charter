package log.charter.services.data;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;

import java.io.File;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Stem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.LoadingDialog;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.ComponentUtils.ConfirmAnswer;
import log.charter.sound.data.AudioData;
import log.charter.util.FileUtils;
import log.charter.util.RW;

public class StemAddService {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ProjectAudioHandler projectAudioHandler;

	private AudioData loadSteamAudio(final File file) {
		final AudioData stemAudioData = LoadingDialog.load(charterFrame, 1, dialog -> {
			dialog.setProgress(0, Label.LOADING_MUSIC_FILE);
			final AudioData result = AudioData.readFile(file);
			dialog.addProgress(Label.LOADING_DONE);

			return result;
		}, "Loading stem audio");

		if (stemAudioData == null) {
			ComponentUtils.showPopup(charterFrame, Label.COULDNT_LOAD_AUDIO, file.getAbsolutePath());
		}

		return stemAudioData;
	}

	private String askForSteamName(final File file) {
		final String fileName = FileUtils.getFileNameWithoutExtension(file);

		String stemName = ComponentUtils.askForInput(charterFrame, Label.AUDIO_STEM_NAME, fileName);
		while (stemName != null && stemName.isBlank()) {
			ComponentUtils.showPopup(charterFrame, Label.AUDIO_STEM_NAME_CANT_BE_EMPTY);
			stemName = ComponentUtils.askForInput(charterFrame, Label.AUDIO_STEM_NAME, fileName);
		}

		return stemName;
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

		final AudioData stemAudioData = loadSteamAudio(file);
		if (stemAudioData == null) {
			return;
		}

		final String stemName = askForSteamName(file);
		if (stemName == null) {
			return;
		}

		addStem(stemName, path, local, stemAudioData);
	}

	private void addStem(final String name, final String path, final boolean local, final AudioData audioData) {
		chartData.songChart.stems.add(new Stem(name, path, local));
		projectAudioHandler.addStem(audioData);
	}
}
