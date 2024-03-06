package log.charter.gui.components.utils;

import log.charter.data.config.Localization.Label;

public class IntegerValueValidator implements ValueValidator {
	private final int minValue;
	private final int maxValue;
	private final boolean acceptEmpty;

	public IntegerValueValidator(final int minValue, final int maxValue, final boolean acceptEmpty) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.acceptEmpty = acceptEmpty;
	}

	@Override
	public String validateValue(final String val) {
		if (((val == null) || val.isEmpty()) && acceptEmpty) {
			return null;
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