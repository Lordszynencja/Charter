package log.charter.gui.components.utils;

import java.io.File;
import java.math.BigDecimal;

import log.charter.data.config.Localization.Label;

public interface ValueValidator {
	public static final ValueValidator dirValidator = val -> {
		final File f = new File(val);
		if (!f.exists()) {
			return Label.DIRECTORY_DOESNT_EXIST.label();
		}

		if (!f.isDirectory()) {
			return Label.NOT_A_FOLDER.label();
		}

		return null;
	};

	public static ValueValidator createBigDecimalValidator(final BigDecimal minVal, final BigDecimal maxVal,
			final boolean acceptEmpty) {
		return val -> {
			if (((val == null) || val.isEmpty()) && acceptEmpty) {
				return null;
			}

			final BigDecimal number;
			try {
				number = new BigDecimal(val);
			} catch (final Exception e) {
				return Label.VALUE_NUMBER_EXPECTED.label();
			}

			if (number.compareTo(minVal) < 0) {
				return String.format(Label.VALUE_MUST_BE_GE.label(), minVal.toString());
			}
			if (number.compareTo(maxVal) > 0) {
				return String.format(Label.VALUE_MUST_BE_LE.label(), maxVal.toString());
			}

			return null;
		};
	}

	public static IntegerValueValidator createIntegerValidator(final int minVal, final int maxVal,
			final boolean acceptEmpty) {
		return new IntegerValueValidator(minVal, maxVal, acceptEmpty);
	}

	String validateValue(String val);
}