package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.math.BigDecimal;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.io.Logger;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioData.DifferentChannelAmountException;
import log.charter.sound.data.AudioData.DifferentSampleRateException;
import log.charter.sound.data.AudioData.DifferentSampleSizesException;
import log.charter.sound.utils.AudioGenerator;

public class AddSilenceInTheBeginningPane extends ParamsPane {
	private static final BigDecimalValueValidator setTimeValidator = new BigDecimalValueValidator(new BigDecimal(0),
			new BigDecimal(600), false);

	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartTimeHandler chartTimeHandler;
	private final ChartData data;
	private final ProjectAudioHandler projectAudioHandler;

	private final BigDecimalValueValidator addTimeValidator;
	private final TextInputWithValidation input;

	private boolean addTime = true;

	private BigDecimal time = null;

	public AddSilenceInTheBeginningPane(final CharterFrame frame, final ChartTimeHandler chartTimeHandler, final ChartData data,
			final ProjectAudioHandler projectAudioHandler) {
		super(frame, Label.ADD_SILENCE_PANE, 300);
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.projectAudioHandler = projectAudioHandler;

		addTimeValidator = new BigDecimalValueValidator(new BigDecimal(-data.beats().get(0).position() / 1000.0),
				new BigDecimal(600), false);

		addLabel(0, 20, Label.ADD_SILENCE_SECONDS, 0);

		addBigDecimalConfigValue(1, 20, 0, null, time, 100, addTimeValidator, val -> time = val, false);
		input = (TextInputWithValidation) getPart(-1);
		addSelectTextOnFocus(input);

		addTypeSelector();

		setOnFinish(this::saveAndExit, null);
		addDefaultFinish(4);
	}

	private void setAddTime(final boolean newAddTime) {
		addTime = newAddTime;

		input.setValidator(addTime ? addTimeValidator : setTimeValidator);
	}

	private void addTypeSelector() {
		final ButtonGroup group = new ButtonGroup();
		final JRadioButton addButton = new JRadioButton();
		addButton.setSelected(true);
		addButton.addActionListener(e -> setAddTime(true));
		group.add(addButton);
		final FieldWithLabel<JRadioButton> addField = new FieldWithLabel<>(Label.ADD_SILENCE_TYPE_ADD, 5, 20, 20,
				addButton, LabelPosition.RIGHT_PACKED);
		add(addField, 20, getY(2), 100, 20);

		final JRadioButton setButton = new JRadioButton();
		setButton.addActionListener(e -> setAddTime(false));
		group.add(setButton);
		final FieldWithLabel<JRadioButton> setField = new FieldWithLabel<>(Label.ADD_SILENCE_TYPE_SET, 5, 20, 20,
				setButton, LabelPosition.RIGHT_PACKED);
		add(setField, 120, getY(2), 100, 20);
	}

	private void addSilence(final double time) {
		final AudioData songMusicData = projectAudioHandler.getAudio();
		final AudioData silenceMusicData = AudioGenerator.generateSilence(time, songMusicData.format.getSampleRate(),
				songMusicData.format.getChannels(), songMusicData.format.getSampleSizeInBits() / 8);
		try {
			final AudioData joined = silenceMusicData.join(songMusicData);

			projectAudioHandler.changeAudio(joined);
			projectAudioHandler.changeStemsOffset(time);
			data.songChart.moveBeats(chartTimeHandler.maxTime(), (int) (time * 1000));
		} catch (final DifferentSampleSizesException | DifferentChannelAmountException
				| DifferentSampleRateException e) {
			Logger.error("Couldn't join audio " + songMusicData.format + " and " + silenceMusicData.format);
		}
	}

	private void removeAudio(final double time) {
		final AudioData editedAudio = projectAudioHandler.getAudio().removeFromStart(time);

		projectAudioHandler.changeAudio(editedAudio);
		projectAudioHandler.changeStemsOffset(-time);
		data.songChart.moveBeats(chartTimeHandler.maxTime(), (int) -(time * 1000));
	}

	private double getTimeChange() {
		if (addTime) {
			return time.doubleValue();
		}

		return time.doubleValue() - data.beats().get(0).position() / 1000.0;
	}

	private void saveAndExit() {
		final double timeChange = getTimeChange();

		if (timeChange > 0) {
			addSilence(timeChange);
		} else {
			removeAudio(-timeChange);
		}
	}
}
