package log.charter.gui.components.utils.validators;

import log.charter.data.config.Localization.Label;

public class DoubleValueValidator implements ValueValidator {
	private final double minValue;
	private final double maxValue;
	private final boolean acceptEmpty;

	public DoubleValueValidator(final double minValue, final double maxValue, final boolean acceptEmpty) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.acceptEmpty = acceptEmpty;
	}

	@Override
	public String validateValue(final String val) {
		if (((val == null) || val.isEmpty()) && acceptEmpty) {
			return null;
		}

		final double v;
		try {
			v = Double.valueOf(val);
		} catch (final NumberFormatException e) {
			return Label.VALUE_NUMBER_EXPECTED.label();
		}

		if (v < minValue) {
			return String.format(Label.VALUE_MUST_BE_GE.label(), minValue + "");
		}

		if (v > maxValue) {
			return String.format(Label.VALUE_MUST_BE_LE.label(), maxValue + "");
		}

		return null;
	}
}