package log.charter.gui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.ParamsPane.BooleanValueSetter;
import log.charter.gui.components.ParamsPane.IntegerValueSetter;
import log.charter.gui.components.ParamsPane.ValueSetter;
import log.charter.gui.components.TextInputWithValidation.StringValueSetter;
import log.charter.gui.components.TextInputWithValidation.ValueValidator;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

public class ComponentWithFields extends Container implements RowedContainer {
	private static final long serialVersionUID = 1L;

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

	private final ArrayList2<Component> components = new ArrayList2<>();
	private final PaneSizes sizes;

	public ComponentWithFields() {
		this(new PaneSizes());
	}

	public ComponentWithFields(final PaneSizes sizes) {
		this.sizes = sizes;
	}

	public int getY(final int row) {
		return sizes.getY(row);
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
		if (label == null) {
			return 0;
		}

		final JLabel labelComponent = new JLabel(label.label(), SwingConstants.LEFT);
		final int labelWidth = labelComponent.getPreferredSize().width;
		add(labelComponent, x, y, labelWidth, 20);

		return labelWidth;
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

			addLabelExact(y, x + 20, value.b);

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

	public void addConfigValue(final int row, final int x, int labelWidth, final Label label, final String val,
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
		add(input, fieldX, y, inputLength, 20);
	}

	@Override
	public ArrayList2<Component> components() {
		return components;
	}

	@Override
	public PaneSizes sizes() {
		return sizes;
	}
}
