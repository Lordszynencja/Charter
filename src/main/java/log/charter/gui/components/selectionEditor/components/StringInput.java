package log.charter.gui.components.selectionEditor.components;

import static log.charter.gui.components.simple.TextInputWithValidation.ValueValidator.createIntValidator;

import java.util.function.Consumer;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;

public class StringInput {

	public static FieldWithLabel<TextInputWithValidation> addField(final CurrentSelectionEditor parent,
			final RowedPosition position, final Consumer<Integer> onChange) {
		final TextInputWithValidation input = new TextInputWithValidation(null, 30, createIntValidator(1, 1, false),
				(final Integer val) -> onChange.accept(val - 1), false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.STRING, 40, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		parent.add(field, position, 90);

		return field;
	}
}
