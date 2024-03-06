package log.charter.gui.components.tabs.selectionEditor.simpleComponents;

import java.util.function.Consumer;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class StringInput {

	public static FieldWithLabel<TextInputWithValidation> addField(final CurrentSelectionEditor parent,
			final RowedPosition position, final Consumer<Integer> onChange) {
		final TextInputWithValidation input = TextInputWithValidation.generateForInt(0, 30, //
				new IntValueValidator(1, 1), v -> onChange.accept(v - 1), false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.STRING, 40, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		parent.add(field, position);

		return field;
	}
}
