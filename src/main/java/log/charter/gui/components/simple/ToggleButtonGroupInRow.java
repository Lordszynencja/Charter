package log.charter.gui.components.simple;

import java.util.List;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.utils.PaneSizes;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.util.CollectionUtils.Pair;

public class ToggleButtonGroupInRow<T extends Enum<T>> {
	private final RowedPanel panel;
	private final ButtonGroup group;

	public ToggleButtonGroupInRow(final RowedPanel parent, final RowedPosition position, final int optionWidth,
			final Label label, final Consumer<T> setter, final List<Pair<T, Label>> values) {
		final int width = optionWidth * values.size();
		final PaneSizes sizes = new PaneSizesBuilder(parent.sizes)//
				.width(width)//
				.rowSpacing(1)//
				.verticalSpace(0)//
				.build();

		panel = new RowedPanel(sizes, 2);

		final RowedPosition positionOnPanel = new RowedPosition(0, sizes);
		final JLabel groupLabel = new JLabel(label.label(), JLabel.LEFT);
		panel.addWithSettingSize(groupLabel, positionOnPanel, width, 0, 20);
		positionOnPanel.newRow();

		group = panel.addToggleButtons(positionOnPanel, optionWidth, null, setter, values);

		parent.addWithSettingSize(panel, position, width, 10, sizes.getHeight(2));
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
