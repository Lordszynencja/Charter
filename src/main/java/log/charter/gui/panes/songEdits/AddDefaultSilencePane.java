package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.sound.data.AudioUtils.generateSilence;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Beat;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioDataShort;

public class AddDefaultSilencePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartTimeHandler chartTimeHandler;
	private final ChartData data;
	private final ProjectAudioHandler projectAudioHandler;

	private int bars = 2;

	public AddDefaultSilencePane(final CharterFrame frame, final ChartTimeHandler chartTimeHandler,
			final ChartData data, final ProjectAudioHandler projectAudioHandler) {
		super(frame, Label.ADD_DEFAULT_SILENCE_PANE, 300);
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.projectAudioHandler = projectAudioHandler;

		addLabel(0, 20, Label.ADD_DEFAULT_SILENCE_BARS, 0);

		addIntConfigValue(1, 20, 0, null, 2, 100, //
				new IntValueValidator(0, 5), val -> bars = val, false);
		final JTextField input = (JTextField) getLastPart();
		addSelectTextOnFocus(input);

		addDefaultFinish(3, this::saveAndExit);
	}

	private void removeAudio(final int movement) {
		final AudioDataShort editedAudio = projectAudioHandler.getAudio().remove(movement / 1000.0);

		projectAudioHandler.changeAudio(editedAudio);
		data.songChart.moveEverythingWithBeats(chartTimeHandler.maxTime(), -movement);
	}

	private void addSilence(final int movement) {
		if (movement < 0) {
			removeAudio(-movement);
			return;
		}

		final AudioDataShort songMusicData = projectAudioHandler.getAudio();
		final AudioDataShort silenceMusicData = generateSilence(movement / 1000.0, songMusicData.sampleRate(),
				songMusicData.channels());
		final AudioDataShort joined = silenceMusicData.join(songMusicData);

		projectAudioHandler.changeAudio(joined);
		data.songChart.moveEverythingWithBeats(chartTimeHandler.maxTime(), movement);
	}

	private void addSilenceAndBars() {
		int movement = 10_000;

		final Beat firstBeat = data.songChart.beatsMap.beats.get(0);
		movement -= firstBeat.position();
		final int firstBarPosition = firstBeat.position();
		final int secondBarPosition = data.songChart.beatsMap.beats.get(firstBeat.beatsInMeasure).position();
		final int barLength = secondBarPosition - firstBarPosition;
		movement += bars * barLength;
		addSilence(movement);

		final int beatsInMeasure = firstBeat.beatsInMeasure;
		for (int bar = 0; bar < bars; bar++) {
			final int barPosition = firstBeat.position() - barLength * (bars - bar);
			for (int i = 0; i < beatsInMeasure; i++) {
				final int beatPosition = barPosition + i * barLength / beatsInMeasure;
				data.songChart.beatsMap.beats
						.add(new Beat(beatPosition, beatsInMeasure, firstBeat.noteDenominator, i == 0));
			}
		}

		data.songChart.beatsMap.beats.sort(IConstantPosition::compareTo);
	}

	private void saveAndExit() {
		if (bars == 0) {
			final Beat firstBeat = data.songChart.beatsMap.beats.get(0);
			addSilence(10_000 - firstBeat.position());
		} else {
			addSilenceAndBars();
		}
	}
}
