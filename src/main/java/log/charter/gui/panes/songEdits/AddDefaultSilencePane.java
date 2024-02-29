package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;
import static log.charter.sound.data.AudioUtils.generateSilence;

import java.io.File;

import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.Beat;
import log.charter.sound.StretchedFileLoader;
import log.charter.sound.data.MusicDataShort;
import log.charter.sound.ogg.OggWriter;
import log.charter.util.RW;

public class AddDefaultSilencePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 300;

		return sizes;
	}

	private final ChartData data;

	private int bars = 2;

	public AddDefaultSilencePane(final CharterFrame frame, final ChartData data) {
		super(frame, Label.ADD_DEFAULT_SILENCE_PANE, getSizes());
		this.data = data;

		addLabel(0, 20, Label.ADD_DEFAULT_SILENCE_BARS);

		addIntegerConfigValue(1, 20, 0, null, 2, 100, createIntValidator(0, 10, false), val -> bars = val, false);
		final JTextField input = (JTextField) components.getLast();
		addSelectTextOnFocus(input);

		addDefaultFinish(3, this::saveAndExit);
	}

	private void removeAudio(final int movement) {
		data.music = data.music.remove(movement / 1000.0);
		data.songChart.beatsMap.songLengthMs = data.music.msLength();

		data.songChart.moveEverything(-movement);
	}

	private void addSilence(final int movement) {
		if (movement < 0) {
			removeAudio(-movement);
			return;
		}

		final MusicDataShort songMusicData = data.music;
		final MusicDataShort silenceMusicData = generateSilence(movement / 1000.0, songMusicData.sampleRate(),
				songMusicData.channels());
		final MusicDataShort joined = silenceMusicData.join(songMusicData);
		data.music = joined;
		data.songChart.beatsMap.songLengthMs = joined.msLength();

		data.songChart.moveEverything(movement);
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

		data.songChart.beatsMap.beats.sort(null);
	}

	private void changeMusicFileNameAndMakeBackupIfNeeded() {
		if (!data.songChart.musicFileName.equals("guitar.ogg")) {
			data.songChart.musicFileName = "guitar.ogg";
		} else {
			RW.writeB(new File(data.path, data.songChart.musicFileName + "_old_" + System.currentTimeMillis() + ".ogg"),
					RW.readB(new File(data.path, data.songChart.musicFileName)));
		}
	}

	private void cleanUp() {
		StretchedFileLoader.stopAllProcesses();
		for (final File oldWav : new File(data.path).listFiles(s -> s.getName().matches("guitar_(tmp|[0-9]*).wav"))) {
			oldWav.delete();
		}
	}

	private void saveAndExit() {
		changeMusicFileNameAndMakeBackupIfNeeded();
		if (bars == 0) {
			final Beat firstBeat = data.songChart.beatsMap.beats.get(0);
			addSilence(10_000 - firstBeat.position());
		} else {
			addSilenceAndBars();
		}

		OggWriter.writeOgg(new File(data.path, data.songChart.musicFileName).getAbsolutePath(), data.music);

		cleanUp();
	}
}
