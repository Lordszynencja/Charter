package log.charter.gui.components;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ButtonGroup;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.RowedPanel.ValueSetter;
import log.charter.util.CollectionUtils.Pair;

public class RadioButtonGroupInRow<T extends Enum<T>> {
	private final RowedPanel panel;
	private final ButtonGroup group;

	public RadioButtonGroupInRow(final RowedPanel parent, final int x, final AtomicInteger row, final int optionwidth,
			final int w, final Label label, final ValueSetter<T> setter, final List<Pair<T, Label>> values) {
		panel = new RowedPanel(w, parent.rowHeight, 2);
		panel.addLabelExact(0, 0, label);
		group = panel.addRadioButtonsExact(20, 10, optionwidth, null, setter, values);

		parent.add(panel, x, parent.getY(row.getAndAdd(2)), w, 50);
	}

	public void setSelected(final T value) {
		if (value == null) {
			group.clearSelection();
			return;
		}

		group.getElements().asIterator().forEachRemaining(muteButton -> {
			if (muteButton.getActionCommand().equals(value.name())) {
				muteButton.setSelected(true);
			}
		});
	}

	public void setVisible(final boolean visibility) {
		panel.setVisible(visibility);
	}
}
