package log.charter.gui.components.tabs.selectionEditor.simpleComponents;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;

import java.util.function.IntConsumer;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class FretInput {
	public static FieldWithLabel<TextInputWithValidation> addField(final CurrentSelectionEditor parent,
			final RowedPosition position, final IntConsumer onChange) {
		final TextInputWithValidation input = generateForInt(0, 30, //
				new IntValueValidator(0, Config.instrument.frets), onChange, false);
		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(Label.FRET, 40, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		parent.add(field, position);

		return field;
	}
}
