package log.charter.gui.panes;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import log.charter.data.Config;
import log.charter.gui.CharterFrame;

public class ParamsPane extends JDialog {

	protected static interface BooleanValueSetter {
		void setValue(boolean val);
	}

	protected static interface StringValueSetter {
		void setValue(String val);
	}

	protected static interface ValueValidator {
		String validateValue(String val);
	}

	protected static final ValueValidator dirValidator = val -> {
		final File f = new File(val);
		if (!f.exists()) {
			return "directory doesn't exist";
		}
		if (!f.isDirectory()) {
			return "given path is not folder";
		}
		return null;
	};

	private static final long serialVersionUID = -3193534671039163160L;

	private static final int OPTIONS_LSPACE = 10;
	private static final int OPTIONS_USPACE = 10;
	private static final int OPTIONS_LABEL_WIDTH = 200;
	private static final int OPTIONS_HEIGHT = 25;
	private static final int OPTIONS_MAX_INPUT_WIDTH = 500;

	protected static ValueValidator createBigDecimalValidator(final BigDecimal minVal, final BigDecimal maxVal,
			final boolean acceptEmpty) {
		return val -> {
			if (((val == null) || val.isEmpty()) && acceptEmpty) {
				return null;
			}
			try {
				final BigDecimal number = new BigDecimal(val);
				if (number.compareTo(minVal) < 0) {
					return "value must be greater than or equal " + minVal;
				}
				if (number.compareTo(maxVal) > 0) {
					return "value must be less than or equal " + maxVal;
				}

				return null;
			} catch (final Exception e) {
				return "number expected";
			}
		};
	}

	protected static ValueValidator createIntValidator(final int minVal, final int maxVal, final boolean acceptEmpty) {
		return val -> {
			if (((val == null) || val.isEmpty()) && acceptEmpty) {
				return null;
			}
			try {
				final int i = Integer.parseInt(val);
				if (i < minVal) {
					return "value must be greater than or equal " + minVal;
				}
				if (i > maxVal) {
					return "value must be less than or equal " + maxVal;
				}
				return null;
			} catch (final Exception e) {
				return "number expected";
			}
		};
	}

	protected static ValueValidator createFloatValidator(final float minVal, final float maxVal,
			final boolean acceptEmpty) {
		return val -> {
			if (((val == null) || val.isEmpty()) && acceptEmpty) {
				return null;
			}
			try {
				final float f = Float.parseFloat(val);
				if (f < minVal) {
					return "value must be greater than or equal " + minVal;
				}
				if (f > maxVal) {
					return "value must be less than or equal " + maxVal;
				}
				return null;
			} catch (final Exception e) {
				return "number expected";
			}
		};
	}

	protected static ValueValidator createIntValidatorWarning(final int minVal, final int maxVal,
			final boolean acceptEmpty) {
		return val -> {
			if (((val == null) || val.isEmpty()) && acceptEmpty) {
				return null;
			}
			try {
				final int i = Integer.parseInt(val);
				if (i < minVal) {
					return "value must be greater than or equal " + minVal;
				}
				if (i > maxVal) {
					return "value must be less than or equal " + maxVal;
				}
				return null;
			} catch (final Exception e) {
				return "number expected";
			}
		};
	}

	protected final List<Component> components = new ArrayList<>();
	private final int w;

	public ParamsPane(final CharterFrame frame, final String title, final int rows) {
		this(frame, title, rows, 700);
	}

	public ParamsPane(final CharterFrame frame, final String title, final int rows, final int width) {
		super(frame, title, true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		setLocation(Config.windowPosX + 100, Config.windowPosY + 100);
		pack();
		final Insets insets = getInsets();
		w = width + insets.left + insets.right;
		final int h = insets.top + insets.bottom + (OPTIONS_USPACE * 2) + (rows * OPTIONS_HEIGHT);
		setSize(w, h);
		setLayout(null);
	}

	protected void add(final JComponent component, final int x, final int y, final int w, final int h) {
		component.setBounds(x, y, w, h);
		final Dimension size = new Dimension(w, h);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);

		add(component);
		components.add(component);
	}

	protected void addButtons(final int row, final ActionListener onSave) {
		this.addButtons(row, onSave, e -> dispose());
	}

	protected void addButtons(final int row, final String name, final ActionListener onSave) {
		this.addButtons(row, name, "Cancel", onSave, e -> dispose());
	}

	protected void addButtons(final int row, final ActionListener onSave, final ActionListener onCancel) {
		addButtons(row, "Save", "Cancel", onSave, onCancel);
	}

	protected void addButtons(final int row, final String button1Label, final String button2Label,
			final ActionListener on1, final ActionListener on2) {
		final JButton button1 = new JButton(button1Label);
		button1.addActionListener(on1);
		add(button1, (w - 300) / 2, OPTIONS_USPACE + (row * OPTIONS_HEIGHT), 100, 25);
		final JButton button2 = new JButton(button2Label);
		button2.addActionListener(on2);
		add(button2, (w - 300) / 2 + 125, OPTIONS_USPACE + (row * OPTIONS_HEIGHT), 100, 25);
	}

	protected void addConfigCheckbox(final int id, final String name, final boolean val,
			final BooleanValueSetter setter) {
		final int y = OPTIONS_USPACE + (id * OPTIONS_HEIGHT);
		final JLabel label = new JLabel(name, SwingConstants.LEFT);
		add(label, OPTIONS_LSPACE, y, OPTIONS_LABEL_WIDTH, OPTIONS_HEIGHT);

		final int fieldX = OPTIONS_LSPACE + OPTIONS_LABEL_WIDTH + 3;
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(val);
		checkbox.addActionListener(a -> setter.setValue(checkbox.isSelected()));

		add(checkbox, fieldX, y, 20, OPTIONS_HEIGHT);
	}

	protected void addConfigValue(final int id, final String name, final Object val, final int inputLength,
			final ValueValidator validator, final StringValueSetter setter, final boolean allowWrong) {
		final int y = OPTIONS_USPACE + (id * OPTIONS_HEIGHT);
		final JLabel label = new JLabel(name, SwingConstants.LEFT);
		add(label, OPTIONS_LSPACE, y, OPTIONS_LABEL_WIDTH, OPTIONS_HEIGHT);

		final int fieldX = OPTIONS_LSPACE + OPTIONS_LABEL_WIDTH + 3;
		final JTextField field = new JTextField(val == null ? "" : val.toString(), inputLength);
		field.getDocument().addDocumentListener(new DocumentListener() {
			JLabel error = null;
			final boolean allowWrongValues = allowWrong;

			@Override
			public void changedUpdate(final DocumentEvent e) {
				if (validator == null) {
					setter.setValue(field.getText());
				} else {
					if (error != null) {
						remove(error);
					}

					error = null;
					final String val = field.getText();

					final String validation = validator.validateValue(val);
					if (validation == null) {
						setter.setValue(val);
					} else {
						error = new JLabel(validation);
						add(error, fieldX + field.getWidth(), y, OPTIONS_LABEL_WIDTH, OPTIONS_HEIGHT);
						if (allowWrongValues) {
							setter.setValue(val);
						}
					}

					repaint();
				}
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				changedUpdate(e);
			}
		});

		final int length = inputLength > OPTIONS_MAX_INPUT_WIDTH ? OPTIONS_MAX_INPUT_WIDTH : inputLength;
		add(field, fieldX, y, length, OPTIONS_HEIGHT);
	}
}
