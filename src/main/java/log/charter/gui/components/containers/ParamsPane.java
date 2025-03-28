package log.charter.gui.components.containers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForBigDecimal;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInteger;
import static log.charter.gui.components.utils.ComponentUtils.setComponentBounds;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

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

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.WindowStateConfig;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.PaneSizes;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.gui.components.utils.validators.IntValueValidator;
import log.charter.gui.components.utils.validators.IntegerValueValidator;
import log.charter.gui.components.utils.validators.ValueValidator;
import log.charter.util.collections.Pair;

public class ParamsPane extends JDialog implements WindowListener {
	public class RowedPanelEmulator extends RowedPanel {
		private static final long serialVersionUID = 1L;

		public RowedPanelEmulator() {
			super(ParamsPane.this.sizes, 0);
		}

		@Override
		public Component getPart(final int id) {
			return ParamsPane.this.getPart(id);
		}

		@Override
		public void remove(final Component comp) {
			ParamsPane.this.remove(comp);
		}

		@Override
		public void repaint() {
			ParamsPane.this.repaint();
		}

		@Override
		public void addWithSettingSize(final Component component, final int x, final int y, final int w, final int h) {
			ParamsPane.this.add(component, x, y, w, h);
		}

		@Override
		public void addWithSettingSizeTop(final Component component, final int x, final int y, final int w,
				final int h) {
			addTop(component, x, y, w, h);
		}

		@Override
		public int addLabel(final int row, final int x, final Label label, final int width) {
			return ParamsPane.this.addLabel(row, x, label, width);
		}
	}

	public static interface ValueSetter<T> {
		void setValue(T val);
	}

	public static interface BooleanValueSetter {
		void setValue(boolean val);
	}

	public static interface IntegerValueSetter {
		void setValue(Integer val);
	}

	public static interface BigDecimalValueSetter {
		void setValue(BigDecimal val);
	}

	private static final long serialVersionUID = -3193534671039163160L;

	private static final int OPTIONS_MAX_INPUT_WIDTH = 500;

	protected final CharterFrame frame;
	protected final Component parent;

	private final List<Component> parts = new ArrayList<>();

	public final PaneSizes sizes;

	private Runnable onSave;
	private Runnable onCancel;

	public ParamsPane(final CharterFrame frame, final Label title, final int width) {
		this(frame, title, new PaneSizesBuilder(width).build());
	}

	public ParamsPane(final CharterFrame frame, final Label title, final PaneSizes sizes) {
		super(frame, title.label(), true);

		this.frame = frame;
		parent = frame;
		this.sizes = sizes;

		pack();

		setSizeWithInsets(sizes.width, 100);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setLayout(null);
	}

	protected void setOnFinish(final Runnable onSave, final Runnable onCancel) {
		setOnFinish(SaverWithStatus.defaultFor(onSave), onCancel);
	}

	protected void setOnFinish(final SaverWithStatus onSave, final Runnable onCancel) {
		this.onSave = onSave == null//
				? () -> { dispose(); }//
				: () -> {
					if (onSave.save()) {
						dispose();
					}
				};
		this.onCancel = onCancel == null ? () -> {} : onCancel;
	}

	public Component getPart(int id) {
		if (id < 0) {
			id += parts.size();
		}
		return parts.get(max(0, min(parts.size() - 1, id)));
	}

	private void setSizeWithInsets(final int newWidth, final int newHeight) {
		final Insets insets = getInsets();
		final int width = newWidth + insets.left + insets.right;
		final int height = newHeight + insets.top + insets.bottom;
		setSize(width, height);
	}

	private void setLocation() {
		setLocation(WindowStateConfig.x + frame.getWidth() / 2 - getWidth() / 2,
				WindowStateConfig.y + frame.getHeight() / 2 - getHeight() / 2);
	}

	public int getY(final int row) {
		return sizes.getY(row);
	}

	public void add(final Component component, final int x, final int y, final int w, final int h) {
		setComponentBounds(component, x, y, w, h);
		add(component);
		parts.add(component);
	}

	public void addTop(final Component component, final int x, final int y, final int w, final int h) {
		setComponentBounds(component, x, y, w, h);
		add(component, 0);
		parts.add(component);
	}

	/**
	 * @return width of created label
	 */
	protected int addLabel(final int row, final int x, final String label, final int width) {
		return addLabelExact(getY(row), x, label, width);
	}

	/**
	 * @return width of created label
	 */
	protected int addLabel(final int row, final int x, final Label label, final int width) {
		return addLabelExact(x, getY(row), label, width);
	}

	/**
	 * @return width of created label
	 */
	protected int addLabelExact(final int x, final int y, final Label label, final int width) {
		if (label == null) {
			return 0;
		}

		return addLabelExact(y, x, label.label(), width);
	}

	/**
	 * @return width of created label
	 */
	protected int addLabelExact(final int y, final int x, final String label, int width) {
		if (label == null) {
			return 0;
		}

		final JLabel labelComponent = new JLabel(label, SwingConstants.LEFT);
		if (width == 0) {
			width = labelComponent.getPreferredSize().width;
		}
		add(labelComponent, x, y, width, 20);

		return width;
	}

	protected void addButtons(final int row, final Label button1Label, final Label button2Label, final Runnable on1,
			final Runnable on2) {
		final int center = sizes.width / 2;
		final int x0 = center - 110;
		final int x1 = center + 10;

		final JButton button1 = new JButton(button1Label.label());
		button1.addActionListener(e -> on1.run());
		add(button1, x0, getY(row), 100, 20);

		final JButton button2 = new JButton(button2Label.label());
		button2.addActionListener(e -> on2.run());
		add(button2, x1, getY(row), 100, 20);
	}

	protected void addConfigCheckbox(final int row, final int x, int labelWidth, final Label label, final boolean val,
			final BooleanValueSetter setter) {
		final int actualLabelWidth = addLabel(row, x, label, 0);

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

	protected <T> void addConfigRadioButtons(final int row, final int x, final int optionWidth, final T val,
			final ValueSetter<T> setter, final List<Pair<T, Label>> values) {
		addConfigRadioButtonsExact(getY(row), x, optionWidth, val, setter, values);
	}

	protected <T> void addConfigRadioButtonsExact(final int y, int x, final int optionWidth, final T val,
			final ValueSetter<T> setter, final List<Pair<T, Label>> values) {
		final ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < values.size(); i++) {
			final Pair<T, Label> value = values.get(i);
			final JRadioButton radioButton = new JRadioButton();
			radioButton.setSelected(value.a.equals(val));
			radioButton.addActionListener(a -> setter.setValue(value.a));
			group.add(radioButton);
			add(radioButton, x, y, 20, 20);

			addLabelExact(x + 20, y, value.b, optionWidth - 20);

			x += optionWidth;
		}
	}

	protected void addBigDecimalConfigValue(final int row, final int x, final int labelWidth, final Label label,
			final BigDecimal value, final int inputLength, final BigDecimalValueValidator validator,
			final Consumer<BigDecimal> setter, final boolean allowWrong) {
		final TextInputWithValidation input = generateForBigDecimal(value, inputLength, validator, setter, allowWrong);
		addConfigValue(row, x, labelWidth, label, input, inputLength);
	}

	protected void addIntConfigValue(final int row, final int x, final int labelWidth, final Label label,
			final int value, final int inputLength, final IntValueValidator validator, final IntConsumer setter,
			final boolean allowWrong) {
		final TextInputWithValidation input = generateForInt(value, inputLength, validator, setter, allowWrong);
		addConfigValue(row, x, labelWidth, label, input, inputLength);
	}

	protected void addIntegerConfigValue(final int row, final int x, final int labelWidth, final Label label,
			final Integer value, final int inputLength, final IntegerValueValidator validator,
			final Consumer<Integer> setter, final boolean allowWrong) {
		final TextInputWithValidation input = generateForInteger(value, inputLength, validator, setter, allowWrong);
		addConfigValue(row, x, labelWidth, label, input, inputLength);
	}

	protected void addStringConfigValue(final int row, final int x, final int labelWidth, final Label label,
			final String value, final int inputLength, final ValueValidator validator, final Consumer<String> setter,
			final boolean allowWrong) {
		final TextInputWithValidation input = new TextInputWithValidation(value, inputLength, validator, setter,
				allowWrong);
		addConfigValue(row, x, labelWidth, label, input, inputLength);
	}

	protected void addConfigValue(final int row, final int x, int labelWidth, final Label label,
			final TextInputWithValidation input, final int inputLength) {
		final int y = getY(row);
		if (label != null) {
			final JLabel labelComponent = new JLabel(label.label(), SwingConstants.LEFT);
			if (labelWidth == 0) {
				labelWidth = labelComponent.getPreferredSize().width;
			}

			labelWidth += 5;

			add(labelComponent, x, y, labelWidth, 20);
		}

		final int fieldX = x + labelWidth;
		final int length = inputLength > OPTIONS_MAX_INPUT_WIDTH ? OPTIONS_MAX_INPUT_WIDTH : inputLength;
		add(input, fieldX, y, length, 20);
	}

	protected void addDefaultFinish(final int row) {
		addDefaultFinish(row, true);
	}

	protected void addDefaultFinish(final int row, final boolean setVisible) {
		final Runnable onCancelWithDispose = () -> {
			onCancel.run();
			dispose();
		};

		addButtons(row, Label.BUTTON_SAVE, Label.BUTTON_CANCEL, onSave, onCancelWithDispose);
		getRootPane().registerKeyboardAction(e -> onCancelWithDispose.run(),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> onSave.run(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		addWindowListener(this);

		setSizeWithInsets(sizes.width, sizes.getHeight(row + 1));
		setLocation();

		validate();
		if (setVisible) {
			setVisible(true);
		}
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		onCancel.run();
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}
}
