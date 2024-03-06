package log.charter.gui.components.simple;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class TextInputWithValidation extends JTextField implements DocumentListener {
	private static final long serialVersionUID = 1L;
	public static final Color errorBackground = new Color(160, 64, 64);
	public static final Color textFieldBorder = ColorLabel.BASE_BORDER.color();

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

		public static IntegerValueValidator createIntValidator(final int minVal, final int maxVal,
				final boolean acceptEmpty) {
			return new IntegerValueValidator(minVal, maxVal, acceptEmpty);
		}

		String validateValue(String val);
	}

	public static class IntegerValueValidator implements ValueValidator {
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

	public static class BigDecimalValueValidator implements ValueValidator {
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

	private boolean error;

	private ValueValidator validator;
	private final Consumer<String> setter;
	private final boolean allowWrongValues;

	private boolean disableEvents;

	public TextInputWithValidation(final String text, final int length, final ValueValidator validator,
			final Consumer<String> setter, final boolean allowWrongValues) {
		super(text, length);
		this.allowWrongValues = allowWrongValues;
		this.validator = validator;
		this.setter = setter;

		getDocument().addDocumentListener(this);
		setBorder(new LineBorder(textFieldBorder, 0));
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent e) {
				setCaretColor(getForeground());
			}
		});
	}

	public TextInputWithValidation(final Integer value, final int length, final IntegerValueValidator validator,
			final Consumer<Integer> setter, final boolean allowWrongValues) {
		this(value == null ? "" : (value + ""), length, validator, (final String s) -> {
			try {
				setter.accept(Integer.valueOf(s));
			} catch (final NumberFormatException e) {
				setter.accept(null);
			}
		}, allowWrongValues);
	}

	public TextInputWithValidation(final Integer value, final int length, final IntegerValueValidator validator,
			final IntConsumer setter, final boolean allowWrongValues) {
		this(value == null ? "" : (value + ""), length, validator, (final String s) -> {
			try {
				setter.accept(Integer.valueOf(s));
			} catch (final NumberFormatException e) {
			}
		}, allowWrongValues);
	}

	public TextInputWithValidation(final BigDecimal value, final int length, final BigDecimalValueValidator validator,
			final Consumer<BigDecimal> setter, final boolean allowWrongValues) {
		this(value == null ? "" : value.toString(), length, validator, (final String s) -> {
			try {
				setter.accept(new BigDecimal(s));
			} catch (final NumberFormatException e) {
				setter.accept(null);
			}
		}, allowWrongValues);
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
		setBorder(new LineBorder(textFieldBorder, 0)); // border color instead of fill
	}

	private void validateValue() {
		final String val = getText();
		final String validation = validator.validateValue(val);
		if (validation == null) {
			setter.accept(val);
			return;
		}

		error = true;
		setToolTipText(validation);
		setBorder(new LineBorder(errorBackground, 2)); // border color instead of fill
		if (allowWrongValues) {
			setter.accept(val);
		}
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (disableEvents) {
			return;
		}

		if (validator == null) {
			setter.accept(getText());
			return;
		}

		if (error) {
			clearError();
		}
		validateValue();

		repaint();
	}

	public void setTextWithoutEvent(final String text) {
		disableEvents = true;
		setText(text);
		clearError();
		disableEvents = false;
	}

	public void setValidator(final ValueValidator validator) {
		this.validator = validator;
		clearError();
	}
}
