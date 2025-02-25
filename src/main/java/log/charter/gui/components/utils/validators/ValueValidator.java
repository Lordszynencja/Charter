package log.charter.gui.components.utils.validators;

import java.io.File;

import log.charter.data.config.Localization.Label;

public interface ValueValidator {
	public static ValueValidator notBlankValidator(final Label emptyValueError) {
		return value -> value == null || value.isBlank() ? emptyValueError.label() : null;
	}

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

	String validateValue(String val);
}