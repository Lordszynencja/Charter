package log.charter.gui.components.selectionEditor.components;

import java.util.function.Consumer;

import javax.swing.JCheckBox;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.RowedPosition;

public class BasicCheckboxInput {
	public static FieldWithLabel<JCheckBox> addField(final RowedPanel parent, final Label label,
			final RowedPosition position, final Consumer<Boolean> onChange) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> onChange.accept(input.isSelected()));
		final FieldWithLabel<JCheckBox> field = new FieldWithLabel<>(label, 60, 20, 20, input,
				LabelPosition.LEFT_CLOSE);
		parent.add(field, position, 90);

		return field;
	}
}
