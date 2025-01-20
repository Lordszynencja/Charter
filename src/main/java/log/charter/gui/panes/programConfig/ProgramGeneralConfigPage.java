package log.charter.gui.panes.programConfig;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.BeatsMap.DistanceType;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.gui.components.utils.validators.ValueValidator;
import log.charter.util.FileChooseUtils;

public class ProgramGeneralConfigPage implements Page {
	private static final int pathInputWidth = 400;

	private String musicPath = Config.musicPath;
	private String songsPath = Config.songsPath;

	private int minNoteDistanceFactor = Config.minNoteDistanceFactor;
	private DistanceType minNoteDistanceType = Config.minNoteDistanceType;
	private int minTailLengthFactor = Config.minTailLengthFactor;
	private DistanceType minTailLengthType = Config.minTailLengthType;

	private boolean selectNotesByTails = Config.selectNotesByTails;
	private int backupDelay = Config.backupDelay;

	private FieldWithLabel<TextInputWithValidation> musicPathField;
	private JButton musicFolderPickerButton;
	private FieldWithLabel<TextInputWithValidation> songsPathField;
	private JButton songsFolderPickerButton;

	private FieldWithLabel<TextInputWithValidation> minNoteDistanceFactorField;
	private CharterSelect<DistanceType> minNoteDistanceTypeField;
	private FieldWithLabel<TextInputWithValidation> minTailLengthFactorField;
	private CharterSelect<DistanceType> minTailLengthTypeField;

	private FieldWithLabel<JCheckBox> selectNotesByTailsField;
	private FieldWithLabel<TextInputWithValidation> backupDelayField;

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addMusicPath(panel, position);
		position.newRow();

		addSongsPath(panel, position);
		position.newRow();
		position.newRow();

		addMinNoteDistance(panel, position);
		position.newRow();

		addMinTailLength(panel, position);
		position.newRow();
		position.newRow();

		addSelectNotesByTails(panel, position);
		position.newRow();

		addBackupDelay(panel, position);
		position.newRow();
	}

	private TextInputWithValidation addPathInput(final String value, final Consumer<String> onchange) {
		return new TextInputWithValidation(value, pathInputWidth, ValueValidator.dirValidator, onchange, false);
	}

	private FieldWithLabel<TextInputWithValidation> generatePathField(final RowedPanel panel,
			final RowedPosition position, final Label label, final TextInputWithValidation input) {
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(label, 100, pathInputWidth, 20,
				input, LabelPosition.LEFT);
		panel.add(field, position);

		return field;
	}

	private void selectFolder(final RowedPanel panel, final String path,
			final FieldWithLabel<TextInputWithValidation> field) {
		final File newFolder = FileChooseUtils.chooseDirectory(panel, path);
		if (newFolder == null) {
			return;
		}

		field.field.setText(newFolder.getAbsolutePath());
	}

	private JButton addPathSelectButton(final RowedPanel panel, final RowedPosition position, final String path,
			final FieldWithLabel<TextInputWithValidation> field) {
		final JButton button = new JButton(Label.SELECT_FOLDER.label());
		button.setSize(100, 20);
		button.addActionListener(e -> selectFolder(panel, path, field));
		panel.add(button, position);

		return button;
	}

	private void addMusicPath(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = addPathInput(musicPath, val -> musicPath = val);
		musicPathField = generatePathField(panel, position, Label.MUSIC_FOLDER, input);
		musicFolderPickerButton = addPathSelectButton(panel, position, musicPath, musicPathField);
	}

	private void addSongsPath(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = addPathInput(songsPath, val -> songsPath = val);
		songsPathField = generatePathField(panel, position, Label.SONGS_FOLDER, input);
		songsFolderPickerButton = addPathSelectButton(panel, position, songsPath, songsPathField);
	}

	private FieldWithLabel<TextInputWithValidation> addDistanceValue(final RowedPanel panel,
			final RowedPosition position, final Label label, final int value, final IntConsumer onChange) {
		final TextInputWithValidation input = generateForInt(value, 50, new IntValueValidator(1, 1000), onChange,
				false);

		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(label, 130, 50, 20, input,
				LabelPosition.LEFT);
		panel.add(field, position);

		return field;
	}

	private CharterSelect<DistanceType> addDistanceTypeSelect(final RowedPanel panel, final RowedPosition position,
			final DistanceType current, final Consumer<DistanceType> onChange) {
		final Stream<DistanceType> possibleValues = Stream.of(DistanceType.MILISECONDS, DistanceType.BEATS,
				DistanceType.NOTES);

		final CharterSelect<DistanceType> select = new CharterSelect<>(possibleValues, current, t -> t.label.label(),
				onChange);
		select.setSize(100, 20);

		panel.add(select, position);

		return select;
	}

	private void addMinNoteDistance(final RowedPanel panel, final RowedPosition position) {
		minNoteDistanceFactorField = addDistanceValue(panel, position, Label.MINIMAL_NOTE_DISTANCE,
				minNoteDistanceFactor, v -> minNoteDistanceFactor = v);
		minNoteDistanceTypeField = addDistanceTypeSelect(panel, position, minNoteDistanceType,
				t -> minNoteDistanceType = t);
	}

	private void addMinTailLength(final RowedPanel panel, final RowedPosition position) {
		minTailLengthFactorField = addDistanceValue(panel, position, Label.MINIMAL_TAIL_LENGTH, minTailLengthFactor,
				v -> minTailLengthFactor = v);
		minTailLengthTypeField = addDistanceTypeSelect(panel, position, minTailLengthType, t -> minTailLengthType = t);
	}

	private void addSelectNotesByTails(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> selectNotesByTails = input.isSelected());
		input.setSelected(selectNotesByTails);

		selectNotesByTailsField = new FieldWithLabel<>(Label.SELECT_NOTES_BY_TAILS, 125, 20, 20, input,
				LabelPosition.LEFT);
		panel.add(selectNotesByTailsField, position);
	}

	private void addBackupDelay(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(backupDelay, 50, new IntValueValidator(30, 3600),
				v -> backupDelay = v, false);

		backupDelayField = new FieldWithLabel<>(Label.BACKUP_DELAY_S, 130, 50, 20, input, LabelPosition.LEFT);
		panel.add(backupDelayField, position);
	}

	@Override
	public Label label() {
		return Label.CONFIG_GENERAL;
	}

	@Override
	public void setVisible(final boolean visibility) {
		musicPathField.setVisible(visibility);
		musicFolderPickerButton.setVisible(visibility);
		songsPathField.setVisible(visibility);
		songsFolderPickerButton.setVisible(visibility);
		minNoteDistanceFactorField.setVisible(visibility);
		minNoteDistanceTypeField.setVisible(visibility);
		minTailLengthFactorField.setVisible(visibility);
		minTailLengthTypeField.setVisible(visibility);
		selectNotesByTailsField.setVisible(visibility);
		backupDelayField.setVisible(visibility);
	}

	public void save() {
		Config.musicPath = musicPath;
		Config.songsPath = songsPath;

		Config.minNoteDistanceType = minNoteDistanceType;
		Config.minNoteDistanceFactor = minNoteDistanceFactor;
		Config.minTailLengthType = minTailLengthType;
		Config.minTailLengthFactor = minTailLengthFactor;

		Config.selectNotesByTails = selectNotesByTails;
		Config.backupDelay = backupDelay;
	}

}
