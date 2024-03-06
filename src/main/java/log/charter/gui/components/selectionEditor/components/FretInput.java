package log.charter.gui.components.selectionEditor.components;

import static log.charter.data.config.Config.frets;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;

import java.util.function.IntConsumer;

import log.charter.data.config.Localization.Label;
import log.charter.gui.components.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.IntValueValidator;
import log.charter.gui.components.utils.RowedPosition;

public class FretInput {
	public static FieldWithLabel<TextInputWithValidation> addField(final CurrentSelectionEditor parent,
			final RowedPosition position, final IntConsumer onChange) {
		final TextInputWithValidation input = generateForInt(0, 30, //
				new IntValueValidator(0, frets), onChange, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.FRET, 40, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		parent.add(field, position);

		return field;
	}
}
