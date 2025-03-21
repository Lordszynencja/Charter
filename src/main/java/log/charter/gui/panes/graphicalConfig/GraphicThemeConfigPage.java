package log.charter.gui.panes.graphicalConfig;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForBigDecimal;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.math.BigDecimal;

import javax.swing.JTextField;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.Theme;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.BigDecimalValueValidator;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class GraphicThemeConfigPage implements Page {
	private static final IntValueValidator editorPartHeightValidator = new IntValueValidator(1, 100);
	private static final IntValueValidator timingHeightValidator = new IntValueValidator(1, 200);
	private static final BigDecimalValueValidator scrollSpeedValidator = new BigDecimalValueValidator(
			new BigDecimal("0.1"), new BigDecimal("2.0"), false);

	private Theme theme = GraphicalConfig.theme;

	private int eventsChangeHeight = GraphicalConfig.eventsChangeHeight;
	private int toneChangeHeight = GraphicalConfig.toneChangeHeight;
	private int fhpInfoHeight = GraphicalConfig.fhpInfoHeight;
	private int noteHeight = GraphicalConfig.noteHeight;
	private int noteWidth = GraphicalConfig.noteWidth;
	private int chordHeight = GraphicalConfig.chordHeight;
	private int handShapesHeight = GraphicalConfig.handShapesHeight;
	private int timingHeight = GraphicalConfig.timingHeight;
	private BigDecimal previewScrollSpeed = BigDecimal.valueOf(GraphicalConfig.previewWindowScrollSpeed);

	private FieldWithLabel<CharterSelect<Theme>> themeField;
	private FieldWithLabel<TextInputWithValidation> noteHeightField;
	private FieldWithLabel<TextInputWithValidation> noteWidthField;
	private FieldWithLabel<TextInputWithValidation> chordHeightField;
	private FieldWithLabel<TextInputWithValidation> eventsChangeHeightField;
	private FieldWithLabel<TextInputWithValidation> toneChangeHeightField;
	private FieldWithLabel<TextInputWithValidation> fhpInfoHeightField;
	private FieldWithLabel<TextInputWithValidation> handShapesHeightField;
	private FieldWithLabel<TextInputWithValidation> timingHeightField;
	private FieldWithLabel<TextInputWithValidation> previewScrollSpeedField;

	@Override
	public Label label() {
		return Label.PAGE_THEME;
	}

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addThemePicker(panel, position);
		position.newRow();

		addNoteHeightInput(panel, position);
		addNoteWidthInput(panel, position);
		position.newRow();

		addEventsChangeHeightField(panel, position);
		addToneChangeHeightField(panel, position);
		position.newRow();

		addFHPInfoHeightField(panel, position);
		addChordHeightField(panel, position);
		position.newRow();

		addHandShapesHeightFieldField(panel, position);
		addTimingHeightFieldField(panel, position);
		position.newRow();

		addScrollSpeedFieldField(panel, position);
	}

	private void onThemeChange(final Theme newTheme) {
		theme = newTheme;

		if (theme == Theme.MODERN) {
			noteWidthField.setVisible(false);
			noteWidthField.field.setText(noteHeight + "");
		} else {
			noteWidthField.setVisible(true);
		}
	}

	private void addThemePicker(final RowedPanel panel, final RowedPosition position) {
		final CharterSelect<Theme> input = new CharterSelect<>(Theme.values(), theme, v -> v.label.label(),
				this::onThemeChange);

		themeField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_THEME, 60, 150, 20, input, LabelPosition.LEFT);
		panel.add(themeField, position);
	}

	private void addEventsChangeHeightField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(eventsChangeHeight, 20, //
				editorPartHeightValidator, i -> eventsChangeHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		eventsChangeHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_EVENTS_CHANGE_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		eventsChangeHeightField.setLocation(10, position.y());
		panel.add(eventsChangeHeightField, position);
	}

	private void addToneChangeHeightField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(toneChangeHeight, 20, //
				editorPartHeightValidator, i -> toneChangeHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		toneChangeHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_TONE_CHANGE_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(toneChangeHeightField, position);
	}

	private void addFHPInfoHeightField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(fhpInfoHeight, 20, editorPartHeightValidator,
				i -> fhpInfoHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		fhpInfoHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_FHP_INFO_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(fhpInfoHeightField, position);
	}

	private void addNoteHeightInput(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(noteHeight, 20, //
				editorPartHeightValidator, this::onNoteHeightChange, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		noteHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_NOTE_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(noteHeightField, position);
	}

	private void onNoteHeightChange(final int newHeight) {
		noteHeight = newHeight;
		if (theme == Theme.MODERN) {
			noteWidthField.field.setText(noteHeight + "");
		}
	}

	private void addChordHeightField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(chordHeight, 20, //
				editorPartHeightValidator, i -> chordHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		chordHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_CHORD_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(chordHeightField, position);
	}

	private void addNoteWidthInput(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(noteWidth, 20, //
				editorPartHeightValidator, i -> noteWidth = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		noteWidthField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_NOTE_WIDTH, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(noteWidthField, position);
	}

	private void addHandShapesHeightFieldField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(handShapesHeight, 20, //
				editorPartHeightValidator, i -> handShapesHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		handShapesHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_HAND_SHAPES_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(handShapesHeightField, position);
	}

	private void addTimingHeightFieldField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(timingHeight, 20, //
				timingHeightValidator, i -> timingHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		timingHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_TIMING_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(timingHeightField, position);
	}

	private void addScrollSpeedFieldField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForBigDecimal(previewScrollSpeed, 20, //
				scrollSpeedValidator, i -> previewScrollSpeed = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		previewScrollSpeedField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_PREVIEW_SCROLL_SPEED, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(previewScrollSpeedField, position);
	}

	@Override
	public void setVisible(final boolean visibility) {
		themeField.setVisible(visibility);
		eventsChangeHeightField.setVisible(visibility);
		toneChangeHeightField.setVisible(visibility);
		fhpInfoHeightField.setVisible(visibility);
		noteHeightField.setVisible(visibility);
		noteWidthField.setVisible(visibility && theme != Theme.MODERN);
		chordHeightField.setVisible(visibility);
		handShapesHeightField.setVisible(visibility);
		timingHeightField.setVisible(visibility);
		previewScrollSpeedField.setVisible(visibility);
	}

	public void save() {
		GraphicalConfig.theme = theme;
		GraphicalConfig.eventsChangeHeight = eventsChangeHeight;
		GraphicalConfig.toneChangeHeight = toneChangeHeight;
		GraphicalConfig.fhpInfoHeight = fhpInfoHeight;
		GraphicalConfig.noteHeight = noteHeight;
		GraphicalConfig.noteWidth = noteWidth;
		GraphicalConfig.chordHeight = chordHeight;
		GraphicalConfig.handShapesHeight = handShapesHeight;
		GraphicalConfig.timingHeight = timingHeight;
		GraphicalConfig.previewWindowScrollSpeed = previewScrollSpeed.doubleValue();
	}

}
