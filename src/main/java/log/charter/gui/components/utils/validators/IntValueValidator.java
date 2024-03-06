package log.charter.gui.components.utils.validators;

import log.charter.data.config.Localization.Label;

public class IntValueValidator implements ValueValidator {
	private final int minValue;
	private final int maxValue;

	public IntValueValidator(final int minValue, final int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	public String validateValue(final String val) {
		if ((val == null) || val.isEmpty()) {
			return Label.VALUE_NUMBER_EXPECTED.label();
		}

		final int i;
		try {
			i = Integer.parseInt(val);
		} catch (final Exception e) {
			return Label.VALUE_NUMBER_EXPECTED.label();
		}

		if (i < minValue) {
			return String.format(Label.VALUE_MUST_BE_GE.label(), minValue + "");
		}

		if (i > maxValue) {
			return String.format(Label.VALUE_MUST_BE_LE.label(), maxValue + "");
		}

		return null;
	}
}