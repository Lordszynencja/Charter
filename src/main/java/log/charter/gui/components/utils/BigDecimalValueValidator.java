package log.charter.gui.components.utils;

import java.math.BigDecimal;

import log.charter.data.config.Localization.Label;

public class BigDecimalValueValidator implements ValueValidator {
	private final BigDecimal minValue;
	private final BigDecimal maxValue;
	private final boolean acceptEmpty;

	public BigDecimalValueValidator(final BigDecimal minValue, final BigDecimal maxValue,
			final boolean acceptEmpty) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.acceptEmpty = acceptEmpty;
	}

	@Override
	public String validateValue(final String val) {
		if (((val == null) || val.isEmpty()) && acceptEmpty) {
			return null;
		}

		final BigDecimal i;
		try {
			i = new BigDecimal(val);
		} catch (final NumberFormatException e) {
			return Label.VALUE_NUMBER_EXPECTED.label();
		}

		if (i.compareTo(minValue) < 0) {
			return String.format(Label.VALUE_MUST_BE_GE.label(), minValue.toString());
		}

		if (i.compareTo(maxValue) > 0) {
			return String.format(Label.VALUE_MUST_BE_LE.label(), maxValue.toString());
		}

		return null;
	}
}