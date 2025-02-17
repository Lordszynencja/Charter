package log.charter.gui.panes.programConfig;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;

import javax.swing.JCheckBox;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class ProgramInstrumentConfigPage implements Page {
	private boolean leftHanded = Config.instrument.leftHanded;
	private int maxStrings = Config.instrument.maxStrings;
	private int frets = Config.instrument.frets;
	private int maxBendValue = Config.instrument.maxBendValue;

	private FieldWithLabel<JCheckBox> leftHandedField;
	private FieldWithLabel<TextInputWithValidation> maxStringsField;
	private FieldWithLabel<TextInputWithValidation> fretsField;
	private FieldWithLabel<TextInputWithValidation> maxBendValueField;

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addLeftHanded(panel, position);
		position.newRow();

		addMaxStrings(panel, position);
		addFrets(panel, position);
		position.newRow();

		addMaxBendValue(panel, position);
	}

	private void addLeftHanded(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> leftHanded = input.isSelected());
		input.setSelected(leftHanded);

		leftHandedField = new FieldWithLabel<>(Label.LEFT_HANDED, 100, 20, 20, input, LabelPosition.LEFT);
		panel.add(leftHandedField, position);
	}

	private void addMaxStrings(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(maxStrings, 50, new IntValueValidator(1, 9),
				v -> maxStrings = v, false);

		maxStringsField = new FieldWithLabel<>(Label.MAX_STRINGS, 100, 50, 20, input, LabelPosition.LEFT);
		panel.add(maxStringsField, position);
	}

	private void addFrets(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(frets, 50, new IntValueValidator(0, 32), v -> frets = v,
				false);

		fretsField = new FieldWithLabel<>(Label.FRETS, 50, 50, 20, input, LabelPosition.LEFT);
		panel.add(fretsField, position);
	}

	private void addMaxBendValue(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(maxBendValue, 50, new IntValueValidator(2, 8),
				v -> maxBendValue = v, false);

		maxBendValueField = new FieldWithLabel<>(Label.MAX_BEND_VALUE, 100, 50, 20, input, LabelPosition.LEFT);
		panel.add(maxBendValueField, position);
	}

	@Override
	public Label label() {
		return Label.CONFIG_INSTRUMENT;
	}

	@Override
	public void setVisible(final boolean visibility) {
		leftHandedField.setVisible(visibility);
		maxStringsField.setVisible(visibility);
		fretsField.setVisible(visibility);
		maxBendValueField.setVisible(visibility);
	}

	public void save() {
		Config.instrument.leftHanded = leftHanded;
		Config.instrument.maxStrings = maxStrings;
		Config.instrument.frets = frets;
		Config.instrument.maxBendValue = maxBendValue;
	}
}
