package log.charter.gui.panes.songEdits;

import static java.lang.Math.abs;
import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Beat;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.io.Logger;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioData.DifferentChannelAmountException;
import log.charter.sound.data.AudioData.DifferentSampleRateException;
import log.charter.sound.data.AudioData.DifferentSampleSizesException;
import log.charter.sound.utils.AudioGenerator;

public class SetDefaultSilencePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartTimeHandler chartTimeHandler;
	private final ChartData data;
	private final ProjectAudioHandler projectAudioHandler;

	private int bars;

	public SetDefaultSilencePane(final CharterFrame frame, final ChartTimeHandler chartTimeHandler,
			final ChartData data, final ProjectAudioHandler projectAudioHandler) {
		super(frame, Label.ADD_DEFAULT_SILENCE_PANE, inputSize * 15);
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.projectAudioHandler = projectAudioHandler;

		addLabel(0, inputSize, Label.ADD_DEFAULT_SILENCE_BARS, inputSize * 12);

		bars = data.songChart.barsAdded == 0 ? 2 : 0;
		addIntConfigValue(0, inputSize * 13, 0, null, bars, inputSize, //
				new IntValueValidator(0, 5), val -> bars = val, false);
		final JTextField input = (JTextField) getPart(-1);
		addSelectTextOnFocus(input);

		setOnFinish(this::saveAndExit, null);
		addDefaultFinish(2);
	}

	private void removeAudio(final double movement) {
		final AudioData editedAudio = projectAudioHandler.getAudio().removeFromStart(movement / 1000.0);

		projectAudioHandler.changeAudio(editedAudio);
		projectAudioHandler.changeStemsOffset(-movement / 1000);
		data.songChart.beatsMap.moveBeats(chartTimeHandler.maxTime(), -movement);
	}

	private void addSilence(final double movement) {
		if (abs(movement) < 0.1) {
			return;
		}

		if (movement < 0) {
			removeAudio(-movement);
			return;
		}

		final AudioData songMusicData = projectAudioHandler.getAudio();
		final AudioData silenceMusicData = AudioGenerator.generateSilence(movement / 1000.0,
				songMusicData.format.getSampleRate(), songMusicData.format.getChannels(),
				songMusicData.format.getSampleSizeInBits() / 8);

		try {
			final AudioData joined = silenceMusicData.join(songMusicData);
			projectAudioHandler.changeAudio(joined);
			projectAudioHandler.changeStemsOffset(movement / 1000);
			data.songChart.beatsMap.moveBeats(chartTimeHandler.maxTime(), movement);
		} catch (final DifferentSampleSizesException | DifferentChannelAmountException
				| DifferentSampleRateException e) {
			Logger.error("Couldn't join audio " + songMusicData.format + " and " + silenceMusicData.format);
		}
	}

	private int addBars() {
		final Beat firstBeat = data.songChart.beatsMap.beats.get(0);
		final int beatsInMeasure = firstBeat.beatsInMeasure;
		final double firstBarPosition = firstBeat.position();
		final double secondBarPosition = data.songChart.beatsMap.beats.get(firstBeat.beatsInMeasure).position();
		final double barLength = secondBarPosition - firstBarPosition;
		int beatsAdded = 0;
		for (int bar = 0; bar < bars; bar++) {
			final double barPosition = firstBeat.position() - barLength * (bars - bar);
			for (int i = 0; i < beatsInMeasure; i++) {
				final double beatPosition = barPosition + i * barLength / beatsInMeasure;
				data.songChart.beatsMap.beats.add(beatsAdded++,
						new Beat(beatPosition, beatsInMeasure, firstBeat.noteDenominator, i == 0));
			}
		}

		return beatsAdded;
	}

	private void addSilenceAndBars() {
		final int beatsAdded = addBars();
		final double movement = Math.ceil(10_000 - data.songChart.beatsMap.beats.get(0).position());
		addSilence(movement);

		data.songChart.beatsMap.beats.sort(IConstantPosition::compareTo);
		data.songChart.moveContent(beatsAdded);
	}

	private void saveAndExit() {
		if (bars == 0) {
			final Beat firstBeat = data.songChart.beatsMap.beats.get(0);
			addSilence(10_000 - firstBeat.position());
		} else {
			addSilenceAndBars();
		}

		data.songChart.barsAdded += bars;
	}
}
