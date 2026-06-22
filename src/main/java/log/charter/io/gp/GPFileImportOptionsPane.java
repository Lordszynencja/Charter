package log.charter.io.gp;

import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;

import javax.swing.JCheckBox;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.GPConfig;
import log.charter.data.config.values.InstrumentConfig;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class GPFileImportOptionsPane extends RowedDialog {
	private static final long serialVersionUID = -1952641299779494076L;

	public static GPFileImportOptions getImportOptions(final CharterFrame frame,
			final boolean importTempoMapDefaultValue) {
		final GPFileImportOptionsPane optionsPane = new GPFileImportOptionsPane(frame, importTempoMapDefaultValue);

		if (optionsPane.cancelled) {
			return null;
		}

		final GPFileImportOptions importOptions = new GPFileImportOptions(optionsPane.importTempoMap(),
				optionsPane.generateFHP(), optionsPane.slideInSize, optionsPane.slideOutSize);
		GPConfig.generateFHP = importOptions.generateFHP;
		GPConfig.slideInSize = importOptions.slideInSize;
		GPConfig.slideOutSize = importOptions.slideOutSize;
		Config.markChanged();

		return importOptions;
	}

	private FieldWithLabel<JCheckBox> importTempoMapField;
	private FieldWithLabel<JCheckBox> generateFHPField;
	private FieldWithLabel<TextInputWithValidation> slideInSizeField;
	private FieldWithLabel<TextInputWithValidation> slideOutSizeField;

	private int slideInSize = GPConfig.slideInSize;
	private int slideOutSize = GPConfig.slideOutSize;

	private boolean cancelled = false;

	public GPFileImportOptionsPane(final CharterFrame frame, final boolean importTempoMapDefaultValue) {
		super(frame, Label.GP_FILE_IMPORT_OPTIONS);

		final RowedPosition position = new RowedPosition(inputSize, panel.sizes);
		addImportTempoMap(position, importTempoMapDefaultValue);
		position.newRow();

		addGenerateFHP(position);
		position.newRow();

		addSlideInSize(position);
		position.newRow();
		addSlideOutSize(position);
		position.newRow();
		position.newRow();

		addDefaultFinish(position.y(), null, SaverWithStatus.defaultFor(this::cancel), true);
	}

	private void addImportTempoMap(final RowedPosition position, final boolean importTempoMapDefaultValue) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(importTempoMapDefaultValue);

		importTempoMapField = new FieldWithLabel<>(Label.IMPORT_TEMPO_MAP, inputSize * 7, inputSize, inputSize,
				checkbox, LabelPosition.LEFT);
		panel.add(importTempoMapField, position);
	}

	private void addGenerateFHP(final RowedPosition position) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(GPConfig.generateFHP);

		generateFHPField = new FieldWithLabel<>(Label.GENERATE_FHP, inputSize * 7, inputSize, inputSize, checkbox,
				LabelPosition.LEFT);
		panel.add(generateFHPField, position);
	}

	private void addSlideInSize(final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(slideInSize, inputSize * 5 / 2,
				new IntValueValidator(1, InstrumentConfig.frets - 1), v -> slideInSize = v, false);

		slideInSizeField = new FieldWithLabel<>(Label.SLIDE_IN_SIZE, inputSize * 7, inputSize, inputSize, input,
				LabelPosition.LEFT);
		panel.add(slideInSizeField, position);
	}

	private void addSlideOutSize(final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(slideOutSize, inputSize * 5 / 2,
				new IntValueValidator(1, InstrumentConfig.frets - 1), v -> slideOutSize = v, false);

		slideOutSizeField = new FieldWithLabel<>(Label.SLIDE_OUT_SIZE, inputSize * 7, inputSize, inputSize, input,
				LabelPosition.LEFT);
		panel.add(slideOutSizeField, position);
	}

	private void cancel() {
		cancelled = true;
	}

	public boolean importTempoMap() {
		return importTempoMapField.field.isSelected();
	}

	public boolean generateFHP() {
		return generateFHPField.field.isSelected();
	}
}
