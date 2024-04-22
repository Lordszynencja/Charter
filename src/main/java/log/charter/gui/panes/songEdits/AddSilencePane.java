package log.charter.gui.panes.songEdits;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.sound.data.AudioUtils.generateSilence;

import java.math.BigDecimal;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.sound.data.AudioDataShort;

public class AddSilencePane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private final ChartTimeHandler chartTimeHandler;
	private final ChartData data;
	private final ProjectAudioHandler projectAudioHandler;

	private boolean addTime = true;

	private BigDecimal time = null;

	public AddSilencePane(final CharterFrame frame, final ChartTimeHandler chartTimeHandler, final ChartData data,
			final ProjectAudioHandler projectAudioHandler) {
		super(frame, Label.ADD_SILENCE_PANE, 300);
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.projectAudioHandler = projectAudioHandler;

		addLabel(0, 20, Label.ADD_SILENCE_SECONDS, 0);

		addBigDecimalConfigValue(1, 20, 0, null, time, 100, //
				new BigDecimalValueValidator(new BigDecimal(0.1), new BigDecimal(60), false), val -> time = val, false);
		final JTextField input = (JTextField) getLastPart();
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
		final FieldWithLabel<JRadioButton> addField = new FieldWithLabel<JRadioButton>(Label.ADD_SILENCE_TYPE_ADD, 5,
				20, 20, addButton, LabelPosition.RIGHT_PACKED);
		add(addField, 20, getY(2), 100, 20);

		final JRadioButton setButton = new JRadioButton();
		setButton.addActionListener(e -> addTime = false);
		group.add(setButton);
		final FieldWithLabel<JRadioButton> setField = new FieldWithLabel<JRadioButton>(Label.ADD_SILENCE_TYPE_SET, 5,
				20, 20, setButton, LabelPosition.RIGHT_PACKED);
		add(setField, 120, getY(2), 100, 20);
	}

	private void addSilence(final double time) {
		final AudioDataShort songMusicData = projectAudioHandler.getAudio();
		final AudioDataShort silenceMusicData = generateSilence(time, songMusicData.sampleRate(),
				songMusicData.channels());
		final AudioDataShort joined = silenceMusicData.join(songMusicData);

		projectAudioHandler.changeAudio(joined);
		data.songChart.moveBeats(chartTimeHandler.maxTime(), (int) (time * 1000));
	}

	private void removeAudio(final double time) {
		final AudioDataShort editedAudio = projectAudioHandler.getAudio().remove(time);

		projectAudioHandler.changeAudio(editedAudio);
		data.songChart.moveBeats(chartTimeHandler.maxTime(), (int) -(time * 1000));
	}

	private void saveAndExit() {
		if (addTime) {
			addSilence(time.doubleValue());
		} else {
			final double timeToAdd = time.doubleValue() - data.beats().get(0).position() / 1000.0;
			if (timeToAdd > 0) {
				addSilence(timeToAdd);
			} else {
				removeAudio(-timeToAdd);
			}
		}
	}
}
