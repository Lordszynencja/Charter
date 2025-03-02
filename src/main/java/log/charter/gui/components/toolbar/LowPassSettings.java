package log.charter.gui.components.toolbar;

import static java.util.Arrays.asList;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.DoubleValueValidator;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.sound.effects.pass.PassFilterAlgorithm;

public class LowPassSettings extends RowedDialog {
	private static final long serialVersionUID = -2133758312693862190L;

	private PassFilterAlgorithm algorithm = Config.passFilters.lowPassAlgorithm;
	private int order = Config.passFilters.lowPassOrder;
	private double frequency = Config.passFilters.lowPassFrequency;
	private double rippleDb = Config.passFilters.lowPassRippleDb;

	@SuppressWarnings("unused")
	private final FieldWithLabel<CharterSelect<PassFilterAlgorithm>> algorithmField;
	@SuppressWarnings("unused")
	private final FieldWithLabel<TextInputWithValidation> orderField;
	@SuppressWarnings("unused")
	private final FieldWithLabel<TextInputWithValidation> frequencyField;
	private final FieldWithLabel<TextInputWithValidation> rippleDbField;

	public LowPassSettings(final CharterFrame charterFrame) {
		super(charterFrame, Label.LOW_PASS_SETTINGS, 300);

		final RowedPosition position = new RowedPosition(50, panel.sizes);

		algorithmField = addAlgorithmSelect(position);
		orderField = addOrderInput(position);
		frequencyField = addFrequencyInput(position);
		rippleDbField = addRippleDbInput(position);

		showAlgorithmDependentFields();

		addDefaultFinish(position.newRow().y(), () -> {
			Config.passFilters.lowPassAlgorithm = algorithm;
			Config.passFilters.lowPassOrder = order;
			Config.passFilters.lowPassFrequency = frequency;
			Config.passFilters.lowPassRippleDb = rippleDb;

			Config.markChanged();

			return true;
		}, null, true);
	}

	private void showAlgorithmDependentFields() {
		switch (algorithm) {
			case CHEBYSHEV_I:
			case CHEBYSHEV_II:
				rippleDbField.setVisible(true);
				break;
			default:
				rippleDbField.setVisible(false);
				break;
		}
	}

	private FieldWithLabel<CharterSelect<PassFilterAlgorithm>> addAlgorithmSelect(final RowedPosition position) {
		final CharterSelect<PassFilterAlgorithm> select = new CharterSelect<>(asList(PassFilterAlgorithm.values()),
				algorithm, v -> v.label, v -> {
					algorithm = v;
					showAlgorithmDependentFields();
				});
		final FieldWithLabel<CharterSelect<PassFilterAlgorithm>> field = new FieldWithLabel<>(
				Label.PASS_FILTER_ALGORITHM, 60, 120, 20, select, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addOrderInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForInt(order, 40,
				new IntValueValidator(1, 999), v -> order = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_ORDER, 60, 40, 20,
				input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addFrequencyInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(frequency, 40,
				new DoubleValueValidator(1, 20_000, false), v -> frequency = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_FREQUENCY, 60, 60,
				20, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addRippleDbInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(rippleDb, 40,
				new DoubleValueValidator(0, 100, false), v -> rippleDb = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_RIPPLE_DB, 60, 60,
				20, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}
}
