package log.charter.gui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.*;

import log.charter.data.config.Localization.Label;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

public class RowedPanel extends JPanel {
	public static interface ValueSetter<T> {
		void setValue(T val);
	}

	public static interface BooleanValueSetter {
		void setValue(boolean val);
	}

	public static interface IntegerValueSetter {
		void setValue(Integer val);
	}

	public static void setComponentBounds(final JComponent component, final int x, final int y, final int w,
			final int h) {
		component.setBounds(x, y, w, h);
		final Dimension size = new Dimension(w, h);
		component.setMinimumSize(size);
		component.setPreferredSize(size);
		component.setMaximumSize(size);
	}

	private static final long serialVersionUID = -3193534671039163160L;

	public final int rowHeight;

	public final ArrayList2<Component> components = new ArrayList2<>();

	public RowedPanel(final int width, final int rowHeight, final int rows) {
		this.rowHeight = rowHeight;

		setLayout(null);
		setSize(width, getY(rows) + rowHeight);
		setMinimumSize(getSize());
		setPreferredSize(getSize());
		setMaximumSize(getSize());
	}

	public int getY(final int row) {
		return rowHeight / 2 + rowHeight * row;
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
	public int addLabel(final int row, final int x, final Label label, final int width) {
		return addLabelExact(getY(row), x, label, width);
	}

	/**
	 * @return width of created label
	 */
	public int addLabelExact(final int y, final int x, final Label label, int width) {
		if (label == null) {
			return 0;
		}

		final JLabel labelComponent = new JLabel(label.label(), SwingConstants.LEFT);
		if (width == 0) {
			width = labelComponent.getPreferredSize().width;
		}
		add(labelComponent, x, y, width, 20);

		return width;
	}

	public void addCheckbox(final int row, final int x, int labelWidth, final Label label, final boolean val,
			final BooleanValueSetter setter) {
		final int actualLabelWidth = addLabel(row, x, label, labelWidth);

		if (labelWidth == 0) {
			labelWidth = actualLabelWidth;
		}
		final int checkboxX = x + labelWidth + 3;
		addCheckbox(row, checkboxX, val, setter);
	}

	public void addCheckbox(final int row, final int x, final boolean val, final BooleanValueSetter setter) {
		addCheckboxExact(getY(row), x, val, setter);
	}

	public void addCheckboxExact(final int y, final int x, final boolean val, final BooleanValueSetter setter) {
		final JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected(val);
		checkbox.addActionListener(a -> setter.setValue(checkbox.isSelected()));
		checkbox.setFocusable(false);

		add(checkbox, x, y, 20, 20);
	}

	public <T extends Enum<T>> ButtonGroup addRadioButtons(final int row, final int x, final int optionWidth,
			final T val, final ValueSetter<T> setter, final List<Pair<T, Label>> values) {
		return addRadioButtonsExact(getY(row), x, optionWidth, val, setter, values);
	}

	public <T extends Enum<T>> ButtonGroup addRadioButtonsExact(final int y, int x, final int optionWidth, final T val,
			final ValueSetter<T> setter, final List<Pair<T, Label>> values) {
		final ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < values.size(); i++) {
			final Pair<T, Label> value = values.get(i);
			final JRadioButton radioButton = new JRadioButton();
			radioButton.setActionCommand(value.a.name());
			radioButton.setSelected(value.a.equals(val));
			radioButton.addActionListener(a -> setter.setValue(value.a));
			group.add(radioButton);
			add(radioButton, x, y, 20, 20);

			addLabelExact(y, x + 20, value.b, optionWidth - 20);

			x += optionWidth;
		}

		return group;
	}

	// added for togglebuttons
	public <T extends Enum<T>> ButtonGroup addToggleButtonsExact(final int y, int x, final int optionWidth, final T val,
				 final ValueSetter<T> setter, final List<Pair<T, Label>> values) {
		final ButtonGroup group = new ButtonGroup();

		for (int i = 0; i < values.size(); i++) {
			final Pair<T, Label> value = values.get(i);
			final JToggleButton toggleButton = new JToggleButton(value.b.label());
			toggleButton.setActionCommand(value.a.name());
			toggleButton.setSelected(value.a.equals(val));
			toggleButton.addActionListener(a -> setter.setValue(value.a));
			group.add(toggleButton);
			add(toggleButton, x, y, optionWidth, 20);

			x += optionWidth;
		}

		return group;
	}

}
