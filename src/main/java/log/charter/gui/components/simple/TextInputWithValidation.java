package log.charter.gui.components.simple;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.gui.components.utils.validators.IntegerValueValidator;
import log.charter.gui.components.utils.validators.ValueValidator;

public class TextInputWithValidation extends JTextField implements DocumentListener {
	private static final long serialVersionUID = 1L;
	public static final Color errorBackground = new Color(160, 64, 64);
	public static final Color textFieldBorder = ColorLabel.BASE_BORDER.color();

	public static TextInputWithValidation generateForInteger(final Integer value, final int length,
			final IntegerValueValidator validator, final Consumer<Integer> setter, final boolean allowWrongValues) {
		return new TextInputWithValidation(value == null ? "" : (value + ""), length, validator,
				setterForInteger(setter), allowWrongValues);
	}

	private static Consumer<String> setterForInteger(final Consumer<Integer> setter) {
		return s -> {
			Integer value;
			try {
				value = Integer.valueOf(s);
			} catch (final NumberFormatException e) {
				value = null;
			}
			setter.accept(value);
		};
	}

	public static TextInputWithValidation generateForInt(final int value, final int length,
			final IntValueValidator validator, final IntConsumer setter, final boolean allowWrongValues) {
		return new TextInputWithValidation(value + "", length, validator, setterForInt(setter), allowWrongValues);
	}

	private static Consumer<String> setterForInt(final IntConsumer setter) {
		return s -> {
			try {
				setter.accept(Integer.valueOf(s));
			} catch (final NumberFormatException e) {
			}
		};
	}

	public static TextInputWithValidation generateForBigDecimal(final BigDecimal value, final int length,
			final BigDecimalValueValidator validator, final Consumer<BigDecimal> setter,
			final boolean allowWrongValues) {
		return new TextInputWithValidation(value + "", length, validator, setterForBigDecimal(setter),
				allowWrongValues);
	}

	private static Consumer<String> setterForBigDecimal(final Consumer<BigDecimal> setter) {
		return s -> {
			BigDecimal value;
			try {
				value = new BigDecimal(s);
			} catch (final NumberFormatException e) {
				value = null;
			}
			setter.accept(value);
		};
	}

	private boolean error;

	private ValueValidator validator;
	private final Consumer<String> setter;
	private final boolean allowWrongValues;

	private boolean disableEvents;

	public TextInputWithValidation(final String text, final int length, final ValueValidator validator,
			final Consumer<String> setter, final boolean allowWrongValues) {
		super(new ComponentDocument(), text, length);
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

	private void clearError() {
		error = false;
		setToolTipText(null);
		setBorder(new LineBorder(textFieldBorder, 0));
	}

	private void validateValue(final String value) {
		if (error) {
			clearError();
		}
		if (validator == null) {
			return;
		}

		final String validation = validator.validateValue(value);
		if (validation == null) {
			return;
		}

		error = true;
		setToolTipText(validation);
		setBorder(new LineBorder(errorBackground, 2));
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		changedUpdate(e);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		if (disableEvents) {
			return;
		}

		final String value = getText();
		validateValue(value);

		if (!error || allowWrongValues) {
			setter.accept(value);
		}

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
