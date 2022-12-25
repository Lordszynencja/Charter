package log.charter.gui.components;

import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.data.config.Localization.Label;

public class TextInputWithValidation extends JTextField implements DocumentListener {
	private static final long serialVersionUID = 1L;

	public static interface StringValueSetter {
		void setValue(String val);
	}

	public static interface ValueValidator {
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

		public static ValueValidator createIntValidator(final int minVal, final int maxVal, final boolean acceptEmpty) {
			return val -> {
				if (((val == null) || val.isEmpty()) && acceptEmpty) {
					return null;
				}

				final int i;
				try {
					i = Integer.parseInt(val);
				} catch (final Exception e) {
					return Label.VALUE_NUMBER_EXPECTED.label();
				}

				if (i < minVal) {
					return String.format(Label.VALUE_MUST_BE_GE.label(), minVal + "");
				}

				if (i > maxVal) {
					return String.format(Label.VALUE_MUST_BE_LE.label(), maxVal + "");
				}

				return null;
			};
		}

		String validateValue(String val);
	}

	private boolean error;
	private Color normalBackgroundColor;

	private final ValueValidator validator;
	private final StringValueSetter setter;
	private final boolean allowWrongValues;

	public TextInputWithValidation(final String text, final int length, final ValueValidator validator,
			final StringValueSetter setter, final boolean allowWrongValues) {
		super(text, length);
		this.allowWrongValues = allowWrongValues;
		this.validator = validator;
		this.setter = setter;

		getDocument().addDocumentListener(this);
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	private void clearError() {
		error = false;
		setToolTipText(null);
		setBackground(normalBackgroundColor);
	}

	private void validateValue() {
		final String val = getText();
		final String validation = validator.validateValue(val);
		if (validation == null) {
			setter.setValue(val);
			return;
		}

		error = true;
		normalBackgroundColor = getBackground();
		setToolTipText(validation);
		setBackground(new Color(160, 64, 64));
		if (allowWrongValues) {
			setter.setValue(val);
		}
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (validator == null) {
			setter.setValue(getText());
			return;
		}

		if (error) {
			clearError();
		}
		validateValue();

		repaint();
	}
}
