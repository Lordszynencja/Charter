package log.charter.gui.components.simple;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ButtonGroup;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.containers.RowedPanel.ValueSetter;
import log.charter.util.CollectionUtils.Pair;

public class ToggleButtonGroupInRow<T extends Enum<T>> {
    private final RowedPanel panel;
    private final ButtonGroup group;

    public ToggleButtonGroupInRow(final RowedPanel parent, final int x, final AtomicInteger row, final int optionWidth,
                                  final int w, final Label label, final ValueSetter<T> setter, final List<Pair<T, Label>> values) {
        panel = new RowedPanel(w, parent.rowHeight, 2);
        panel.addLabelExact(0, 0, label, optionWidth * values.size());
        group = panel.addToggleButtonsExact(20, 10, optionWidth, null, setter, values);

        parent.add(panel, x, parent.getY(row.getAndAdd(2)), w, 50);
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
