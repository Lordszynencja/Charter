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

public class BandPassSettings extends RowedDialog {
	private static final long serialVersionUID = -2133758312693862190L;

	private static final int labelWidth = 100;

	private PassFilterAlgorithm algorithm = Config.passFilters.bandPassAlgorithm;
	private int order = Config.passFilters.bandPassOrder;
	private double centerFrequency = Config.passFilters.bandPassCenterFrequency;
	private double frequencyWidth = Config.passFilters.bandPassFrequencyWidth;
	private double rippleDb = Config.passFilters.bandPassRippleDb;

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
		super(charterFrame, Label.BAND_PASS_SETTINGS, 300);

		final RowedPosition position = new RowedPosition(30, panel.sizes);

		algorithmField = addAlgorithmSelect(position);
		orderField = addOrderInput(position);
		centerFrequencyField = addCenterFrequencyInput(position);
		frequencyWidthField = addFrequencyWidthInput(position);
		rippleDbField = addRippleDbInput(position);

		showAlgorithmDependentFields();

		addDefaultFinish(position.newRow().y(), () -> {
			Config.passFilters.bandPassAlgorithm = algorithm;
			Config.passFilters.bandPassOrder = order;
			Config.passFilters.bandPassCenterFrequency = centerFrequency;
			Config.passFilters.bandPassFrequencyWidth = frequencyWidth;
			Config.passFilters.bandPassRippleDb = rippleDb;

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
				Label.PASS_FILTER_ALGORITHM, labelWidth, 120, 20, select, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addOrderInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForInt(order, 40,
				new IntValueValidator(1, 999), v -> order = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_ORDER, labelWidth,
				40, 20, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addCenterFrequencyInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(centerFrequency, 40,
				new DoubleValueValidator(1, 20_000, false), v -> centerFrequency = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_CENTER_FREQUENCY,
				labelWidth, 60, 20, input, LabelPosition.LEFT);
		panel.add(field, position);
		position.newRow();

		return field;
	}

	private FieldWithLabel<TextInputWithValidation> addFrequencyWidthInput(final RowedPosition position) {
		final TextInputWithValidation input = TextInputWithValidation.generateForDouble(frequencyWidth, 40,
				new DoubleValueValidator(1, 20_000, false), v -> frequencyWidth = v, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.PASS_FILTER_FREQUENCY_WIDTH,
				labelWidth, 60, 20, input, LabelPosition.LEFT);
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
