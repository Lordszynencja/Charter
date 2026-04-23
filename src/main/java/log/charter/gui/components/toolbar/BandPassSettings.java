package log.charter.gui.components.toolbar;

import static java.util.Arrays.asList;
import static log.charter.data.config.GraphicalConfig.inputSize;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.PassFiltersConfig;
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

public class BandPassSettings extends RowedDialog {
	private static final long serialVersionUID = -2133758312693862190L;

	private final int labelWidth = inputSize * 5;

	private PassFilterAlgorithm algorithm = PassFiltersConfig.bandPassAlgorithm;
	private int order = PassFiltersConfig.bandPassOrder;
	private double centerFrequency = PassFiltersConfig.bandPassCenterFrequency;
	private double frequencyWidth = PassFiltersConfig.bandPassFrequencyWidth;
	private double rippleDb = PassFiltersConfig.bandPassRippleDb;

	@SuppressWarnings("unused")
	private final FieldWithLabel<CharterSelect<PassFilterAlgorithm>> algorithmField;
	@SuppressWarnings("unused")
	private final FieldWithLabel<TextInputWithValidation> orderField;
	@SuppressWarnings("unused")
	private final FieldWithLabel<TextInputWithValidation> centerFrequencyField;
	@SuppressWarnings("unused")
	private final FieldWithLabel<TextInputWithValidation> frequencyWidthField;
	private final FieldWithLabel<TextInputWithValidation> rippleDbField;

	public BandPassSettings(final CharterFrame charterFrame) {
		super(charterFrame, Label.BAND_PASS_SETTINGS, inputSize * 15);

		final RowedPosition position = new RowedPosition(inputSize * 3 / 2, panel.sizes);

		algorithmField = addAlgorithmSelect(position);
		orderField = addOrderInput(position);
		centerFrequencyField = addCenterFrequencyInput(position);
		frequencyWidthField = addFrequencyWidthInput(position);
		rippleDbField = addRippleDbInput(position);

		showAlgorithmDependentFields();

		addDefaultFinish(position.newRow().y(), () -> {
			PassFiltersConfig.bandPassAlgorithm = algorithm;
			PassFiltersConfig.bandPassOrder = order;
			PassFiltersConfig.bandPassCenterFrequency = centerFrequency;
			PassFiltersConfig.bandPassFrequencyWidth = frequencyWidth;
			PassFiltersConfig.bandPassRippleDb = rippleDb;

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
				Label.PASS_FILTER_ALGORITHM, labelWidth, inputSize * 6, inputSize, select, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addOrderInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForInt(order, inputSize * 2,
				new IntValueValidator(1, 999), v -> order = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_ORDER, labelWidth,
				inputSize, inputSize, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addCenterFrequencyInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(centerFrequency, inputSize * 2,
				new DoubleValueValidator(1, 20_000, false), v -> centerFrequency = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_CENTER_FREQUENCY,
				labelWidth, inputSize * 2, inputSize, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addFrequencyWidthInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(frequencyWidth, inputSize * 2,
				new DoubleValueValidator(1, 20_000, false), v -> frequencyWidth = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_FREQUENCY_WIDTH,
				labelWidth, inputSize * 2, inputSize, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addRippleDbInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(rippleDb, inputSize * 2,
				new DoubleValueValidator(0, 100, false), v -> rippleDb = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_RIPPLE_DB,
				labelWidth, inputSize * 2, inputSize, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}
}
