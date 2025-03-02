package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.io.Logger;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioData;
import log.charter.sound.data.AudioData.DifferentChannelAmountException;
import log.charter.sound.data.AudioData.DifferentSampleRateException;
import log.charter.sound.data.AudioData.DifferentSampleSizesException;
import log.charter.sound.utils.AudioGenerator;

public class AddSilenceAtTheEndPane extends RowedDialog {

	private static final long serialVersionUID = -4754359602173894487L;

	private final ProjectAudioHandler projectAudioHandler;

	private final double audioLength;

	private JLabel label;
	private BigDecimalValueValidator changeLengthValidator;
	private BigDecimalValueValidator setLengthValidator;
	private TextInputWithValidation input;

	private double time = 0;
	private boolean setLength = false;

	public AddSilenceAtTheEndPane(final CharterFrame frame, final ProjectAudioHandler projectAudioHandler) {
		super(frame, Label.ADD_SILENCE_AT_THE_END, 100);
		this.projectAudioHandler = projectAudioHandler;

		audioLength = projectAudioHandler.audioLengthMs() / 1000;

		final RowedPosition position = new RowedPosition(20, panel.sizes);

		addLabel(position);

		position.newRow();
		addInput(position);

		position.newRow();
		addTypeSelector(position);

		position.newRow();
		position.newRow();
		addDefaultFinish(position.y(), SaverWithStatus.defaultFor(this::saveAndExit), null, true);
	}

	private void addLabel(final RowedPosition position) {
		label = new JLabel(Label.CHANGE_LENGTH_BY_SECONDS.label());

		panel.addWithSettingSize(label, position, 250);
	}

	private BigDecimal getTime() {
		return new BigDecimal(time).setScale(3, RoundingMode.HALF_UP);
	}

	private void setTime(final BigDecimal newTime) {
		time = newTime.doubleValue();
	}

	private void addInput(final RowedPosition position) {
		changeLengthValidator = new BigDecimalValueValidator(new BigDecimal(-audioLength), new BigDecimal(30), false);
		setLengthValidator = new BigDecimalValueValidator(new BigDecimal(0), new BigDecimal(audioLength + 30), false);

		input = TextInputWithValidation.generateForBigDecimal(getTime(), 100, changeLengthValidator, this::setTime,
				false);
		addSelectTextOnFocus(input);

		panel.addWithSettingSize(input, position, 100);
	}

	private void setSetLength(final boolean newSetLength) {
		setLength = newSetLength;

		final Label currentLabel = setLength ? Label.SET_LENGTH_TO_SECONDS : Label.CHANGE_LENGTH_BY_SECONDS;
		label.setText(currentLabel.label());
		label.repaint();

		time = setLength ? projectAudioHandler.audioLengthMs() / 1000 : 0;

		input.setValidator(setLength ? setLengthValidator : changeLengthValidator);
		input.setText(getTime().toString());
		input.repaint();
	}

	private void addTypeSelector(final RowedPosition position) {
		final ButtonGroup group = new ButtonGroup();
		final JRadioButton changeButton = new JRadioButton();
		changeButton.setSelected(true);
		changeButton.addActionListener(e -> setSetLength(false));
		group.add(changeButton);
		final FieldWithLabel<JRadioButton> changeField = new FieldWithLabel<>(Label.CHANGE_LENGTH, 0, 20, 20,
				changeButton, LabelPosition.RIGHT_PACKED);
		panel.add(changeField, position);

		final JRadioButton setButton = new JRadioButton();
		setButton.addActionListener(e -> setSetLength(true));
		group.add(setButton);
		final FieldWithLabel<JRadioButton> setField = new FieldWithLabel<>(Label.SET_LENGTH, 0, 20, 20, setButton,
				LabelPosition.RIGHT_PACKED);
		panel.add(setField, position);
	}

	private void addSilence(final double time) {
		final AudioData songMusicData = projectAudioHandler.getAudio();
		final AudioData silenceMusicData = AudioGenerator.generateSilence(time, songMusicData.format.getSampleRate(),
				songMusicData.format.getChannels(), songMusicData.format.getSampleSizeInBits() / 8);
		try {
			final AudioData joined = songMusicData.join(silenceMusicData);

			projectAudioHandler.changeAudio(joined);
		} catch (final DifferentSampleSizesException | DifferentChannelAmountException
				| DifferentSampleRateException e) {
			Logger.error("Couldn't join audio " + songMusicData.format + " and " + silenceMusicData.format);
		}
	}

	private double getLength() {
		if (setLength) {
			return time;
		}

		return time + audioLength;
	}

	private void cutAudio(final double length) {
		final AudioData editedAudio = projectAudioHandler.getAudio().cutToLength(length);
		projectAudioHandler.changeAudio(editedAudio);
	}

	private void saveAndExit() {
		final double newLength = getLength();

		if (newLength > audioLength) {
			addSilence(newLength - audioLength);
		} else {
			cutAudio(newLength);
		}
	}
}
