package log.charter.gui.components.simple;

import static log.charter.data.config.GraphicalConfig.inputSize;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.util.collections.Pair;

public class ToggleButtonGroupInRow<T extends Enum<T>> {
	private final JLabel label;
	private final ButtonGroup group;
	private final List<JToggleButton> buttons;

	public ToggleButtonGroupInRow(final Container parent, final Label label, final Consumer<T> setter,
			final List<Pair<T, Label>> values) {
		this.label = new JLabel(label.label(), JLabel.LEFT);
		parent.add(this.label);

		group = new ButtonGroup();
		buttons = new ArrayList<>(values.size());

		for (int i = 0; i < values.size(); i++) {
			final Pair<T, Label> value = values.get(i);

			final JToggleButton toggleButton = new JToggleButton(value.b.label());
			toggleButton.setFocusable(false);
			toggleButton.setActionCommand(value.a.name());
			toggleButton.addActionListener(a -> setter.accept(value.a));
			toggleButton.setSelected(false);
			group.add(toggleButton);
			buttons.add(toggleButton);
			parent.add(toggleButton);
		}
	}

	public void setSelected(final T value) {
		if (value == null) {
			group.clearSelection();
			return;
		}

		group.getElements().asIterator().forEachRemaining(toggleButton -> {
			if (toggleButton.getActionCommand().equals(value.name())) {
				toggleButton.setSelected(true);
			}
		});
	}

	public void setVisible(final boolean visibility) {
		label.setVisible(visibility);
		buttons.forEach(b -> b.setVisible(visibility));
	}

	public void recalculateSizes(final int x, final int y, final int optionsWidth) {
		ComponentUtils.resize(label, x, y, optionsWidth);

		for (int i = 0; i < buttons.size(); i++) {
			final int from = optionsWidth * i / buttons.size();
			final int to = optionsWidth * (i + 1) / buttons.size() - 1;
			ComponentUtils.resize(buttons.get(i), x + from, y + inputSize, to - from);
		}
	}
}
