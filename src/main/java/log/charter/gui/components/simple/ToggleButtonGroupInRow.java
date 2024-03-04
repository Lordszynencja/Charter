package log.charter.gui.components.simple;

import java.util.List;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.util.CollectionUtils.Pair;

public class ToggleButtonGroupInRow<T extends Enum<T>> {
	private final RowedPanel panel;
	private final ButtonGroup group;

	public ToggleButtonGroupInRow(final RowedPanel parent, final RowedPosition position, final int optionWidth,
			final int w, final Label label, final Consumer<T> setter, final List<Pair<T, Label>> values) {
		panel = new RowedPanel(parent.sizes, 2);
		panel.addLabelExact(0, 0, label, optionWidth * values.size());
		group = panel.addToggleButtons(position, optionWidth, null, setter, values);

		parent.addWithSettingSize(panel, position, w, 10, parent.sizes.getHeight(2));
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
		panel.setVisible(visibility);
	}
}
