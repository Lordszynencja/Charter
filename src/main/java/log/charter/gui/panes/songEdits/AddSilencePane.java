package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createBigDecimalValidator;
import static log.charter.sound.data.AudioUtils.generateSilence;

import java.io.File;
import java.math.BigDecimal;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.ParamsPane;
import log.charter.sound.StretchedFileLoader;
import log.charter.sound.data.MusicDataShort;
import log.charter.sound.ogg.OggWriter;
import log.charter.util.RW;

public class AddSilencePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 80;
		sizes.width = 300;

		return sizes;
	}

	private final ChartData data;

	private boolean addTime = true;

	private BigDecimal time;

	public AddSilencePane(final CharterFrame frame, final ChartData data) {
		super(frame, Label.ADD_SILENCE_PANE, getSizes());
		this.data = data;

		addLabel(0, 20, Label.ADD_SILENCE_SECONDS);

		addConfigValue(1, 20, 0, null, "", 100,
				createBigDecimalValidator(new BigDecimal(0.1), new BigDecimal(60), false),
				val -> time = new BigDecimal(val), false);
		final JTextField input = (JTextField) components.getLast();
		addSelectTextOnFocus(input);

		addTypeSelector();

		addDefaultFinish(4, this::saveAndExit);
	}

	private void addTypeSelector() {
		final ButtonGroup group = new ButtonGroup();
		final JRadioButton addButton = new JRadioButton();
		addButton.setSelected(true);
		addButton.addActionListener(e -> addTime = true);
		group.add(addButton);
		final FieldWithLabel<JRadioButton> addField = new FieldWithLabel<JRadioButton>(Label.ADD_SILENCE_ADD, 5, 20, 20,
				addButton, LabelPosition.RIGHT_PACKED);
		add(addField, 20, getY(2), 100, 20);

		final JRadioButton setButton = new JRadioButton();
		setButton.addActionListener(e -> addTime = false);
		group.add(setButton);
		final FieldWithLabel<JRadioButton> setField = new FieldWithLabel<JRadioButton>(Label.ADD_SILENCE_SET, 5, 20, 20,
				setButton, LabelPosition.RIGHT_PACKED);
		add(setField, 120, getY(2), 100, 20);
	}

	private void addSilence(final double time) {
		final MusicDataShort songMusicData = data.music;
		final MusicDataShort silenceMusicData = generateSilence(time, songMusicData.sampleRate(),
				songMusicData.channels());
		final MusicDataShort joined = silenceMusicData.join(songMusicData);
		data.music = joined;
		data.songChart.beatsMap.songLengthMs = joined.msLength();

		data.songChart.moveEverything((int) (time * 1000));
	}

	private void removeAudio(final double time) {
		data.music = data.music.remove(time);
		data.songChart.beatsMap.songLengthMs = data.music.msLength();

		data.songChart.moveEverything((int) -(time * 1000));
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

		if (addTime) {
			addSilence(time.doubleValue());
		} else {
			final double timeToAdd = time.doubleValue() - data.songChart.beatsMap.beats.get(0).position() / 1000.0;
			if (timeToAdd > 0) {
				addSilence(timeToAdd);
			} else {
				removeAudio(-timeToAdd);
			}
		}

		OggWriter.writeOgg(new File(data.path, data.songChart.musicFileName).getAbsolutePath(), data.music);

		cleanUp();
	}
}
