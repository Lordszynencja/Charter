package log.charter.gui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.TextInputWithValidation.StringValueSetter;
import log.charter.gui.components.TextInputWithValidation.ValueValidator;
import log.charter.util.CollectionUtils.ArrayList2;

public class ParamsPane extends JDialog {
	public static class PaneSizes {
		public int width = 700;
		public int lSpace = 20;
		public int uSpace = 10;
		public int labelWidth = 200;
		public int rowHeight = 25;

		public int getY(final int row) {
			return uSpace + row * rowHeight;
		}
	}

	public static interface BooleanValueSetter {
		void setValue(boolean val);
	}

	public static interface IntegerValueSetter {
		void setValue(Integer val);
	}

	private static final long serialVersionUID = -3193534671039163160L;

	private static final int OPTIONS_MAX_INPUT_WIDTH = 500;

	protected final ArrayList2<Component> components = new ArrayList2<>();

	private final int width;
	protected final PaneSizes sizes;

	public ParamsPane(final CharterFrame frame, final String title, final int rows) {
		this(frame, title, rows, new PaneSizes());
	}

	protected int getY(final int row) {
		return sizes.getY(row);
	}

	public ParamsPane(final CharterFrame frame, final String title, final int rows, final PaneSizes sizes) {
		super(frame, title, true);
		this.sizes = sizes;

		pack();

		final Insets insets = getInsets();
		width = sizes.width + insets.left + insets.right;
		final int h = insets.top + insets.bottom + (sizes.uSpace * 2) + (rows * sizes.rowHeight);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		setLocation(Config.windowPosX + frame.getWidth() / 2 - width / 2,
				Config.windowPosY + frame.getHeight() / 2 - h / 2);

		setSize(width, h);
		setLayout(null);
	}

	private void setComponentBounds(final JComponent component, final int x, final int y, final int w, final int h) {
		component.setBounds(x, y, w, h);
		final Dimension size = new Dimension(w, h);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
	}

	public void add(final JComponent component, final int x, final int y, final int w, final int h) {
		setComponentBounds(component, x, y, w, h);
		add(component);
		components.add(component);
	}

	public void addTop(final JComponent component, final int x, final int y, final int w, final int h) {
		setComponentBounds(component, x, y, w, h);
		add(component, 0);
		components.add(component);
	}

	/**
	 * @return width of created label
	 */
	protected int addLabel(final int row, final int x, final Label label) {
		return addLabelExact(getY(row), x, label);
	}

	/**
	 * @return width of created label
	 */
	protected int addLabelExact(final int y, final int x, final Label label) {
		final JLabel labelComponent = new JLabel(label.label(), SwingConstants.LEFT);
		final int labelWidth = labelComponent.getPreferredSize().width;
		add(labelComponent, x, y, labelWidth, 20);

		return labelWidth;
	}

	protected void addDefaultFinish(final int row, final Runnable onSave) {
		addDefaultFinish(row, onSave, () -> {
		});
	}

	protected void addDefaultFinish(final int row, final Runnable onSave, final Runnable onCancel) {
		final Runnable paneOnSave = () -> {
			onSave.run();
			dispose();
		};
		final Runnable paneOnCancel = () -> {
			onCancel.run();
			dispose();
		};

		addButtons(row, paneOnSave, paneOnCancel);
		getRootPane().registerKeyboardAction(e -> paneOnCancel.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> paneOnSave.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	protected void addButtons(final int row, final Runnable onSave) {
		this.addButtons(row, onSave, this::dispose);
	}

	protected void addButtons(final int row, final Label label, final Runnable onSave) {
		this.addButtons(row, label, Label.BUTTON_CANCEL, onSave, this::dispose);
	}

	protected void addButtons(final int row, final Runnable onSave, final Runnable onCancel) {
		addButtons(row, Label.BUTTON_SAVE, Label.BUTTON_CANCEL, onSave, onCancel);
	}

	protected void addButtons(final int row, final Label button1Label, final Label button2Label, final Runnable on1,
			final Runnable on2) {
		final int center = width / 2;
		final int x0 = center - 100 - 20;
		final int x1 = center + 10;

		final JButton button1 = new JButton(button1Label.label());
		button1.addActionListener(e -> on1.run());
		add(button1, x0, getY(row), 100, 20);

		final JButton button2 = new JButton(button2Label.label());
		button2.addActionListener(e -> on2.run());
		add(button2, x1, getY(row), 100, 20);
	}

	protected void addConfigCheckbox(final int row, final Label label, final boolean val,
			final BooleanValueSetter setter) {
		addConfigCheckbox(row, sizes.lSpace, sizes.labelWidth, label, val, setter);
	}

	protected void addConfigCheckbox(final int row, final int x, int labelWidth, final Label label, final boolean val,
			final BooleanValueSetter setter) {
		final int actualLabelWidth = addLabel(row, x, label);

		if (labelWidth == 0) {
			labelWidth = actualLabelWidth;
		}
		final int checkboxX = x + labelWidth + 3;
		addConfigCheckbox(row, checkboxX, val, setter);
	}

	protected void addConfigCheckbox(final int row, final int x, final boolean val, final BooleanValueSetter setter) {
		addConfigCheckboxExact(getY(row), x, val, setter);
	}

	protected void addConfigCheckboxExact(final int y, final int x, final boolean val,
			final BooleanValueSetter setter) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(val);
		checkbox.addActionListener(a -> setter.setValue(checkbox.isSelected()));
		checkbox.setFocusable(false);

		add(checkbox, x, y, 20, 20);
	}

	protected <T extends Enum<T>> void addConfigRadioButtons(final int row, final int x, final int optionWidth,
			final int val, final IntegerValueSetter setter, final Label... labels) {
		addConfigRadioButtonsExact(getY(row), x, optionWidth, val, setter, labels);
	}

	protected <T extends Enum<T>> void addConfigRadioButtonsExact(final int y, int x, final int optionWidth,
			final int val, final IntegerValueSetter setter, final Label... labels) {
		final ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < labels.length; i++) {
			final JRadioButton radioButton = new JRadioButton();
			radioButton.setSelected(i == val);
			final int buttonId = i;
			radioButton.addActionListener(a -> setter.setValue(buttonId));
			group.add(radioButton);
			add(radioButton, x, y, 20, 20);

			addLabelExact(y, x + 20, labels[i]);

			x += optionWidth;
		}
	}

	protected void addIntegerConfigValue(final int row, final int x, final int labelWidth, final Label label,
			final Integer value, final int inputLength, final ValueValidator validator, final IntegerValueSetter setter,
			final boolean allowWrong) {
		addConfigValue(row, x, labelWidth, label, value == null ? "" : value.toString(), inputLength, validator,
				val -> {
					try {
						setter.setValue(Integer.valueOf(val));
					} catch (final NumberFormatException e) {
						setter.setValue(null);
					}
				}, allowWrong);
	}

	protected void addConfigValue(final int row, final Label label, final String val, final int inputLength,
			final ValueValidator validator, final StringValueSetter setter, final boolean allowWrong) {
		addConfigValue(row, sizes.lSpace, sizes.labelWidth, label, val, inputLength, validator, setter, allowWrong);
	}

	protected void addConfigValue(final int row, final int x, int labelWidth, final Label label, final String val,
			final int inputLength, final ValueValidator validator, final StringValueSetter setter,
			final boolean allowWrong) {
		final int y = getY(row);
		if (label != null) {
			final JLabel labelComponent = new JLabel(label.label(), SwingConstants.LEFT);
			if (labelWidth == 0) {
				labelWidth = labelComponent.getPreferredSize().width;
			}

			labelWidth += 5;

			add(labelComponent, x, y, labelWidth, 20);
		}

		final TextInputWithValidation input = new TextInputWithValidation(val, inputLength, validator, setter,
				allowWrong);

		final int fieldX = x + labelWidth;
		final int length = inputLength > OPTIONS_MAX_INPUT_WIDTH ? OPTIONS_MAX_INPUT_WIDTH : inputLength;
		add(input, fieldX, y, length, 20);
	}
}
