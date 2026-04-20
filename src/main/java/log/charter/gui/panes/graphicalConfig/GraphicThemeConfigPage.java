package log.charter.gui.panes.graphicalConfig;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForBigDecimal;
import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;
import static log.charter.gui.components.utils.ComponentUtils.numberFilter;
import static log.charter.gui.components.utils.ComponentUtils.setDefaultFontSize;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
	private static final Label[] sizeLabels = { Label.SIZE_S, Label.SIZE_M, Label.SIZE_L, Label.SIZE_XL,
			Label.SIZE_XXL };
	private static final int[] sizesChartMap = { 2, 3, 4, 6, 8 };
	private static final int[] sizesText = { 7, 10, 15, 20, 25 };
	private static final int[] sizesTiming = { 16, 24, 36, 48, 60 };
	private static final int[] sizesNotes = { 17, 25, 37, 51, 63 };

	private static final IntValueValidator editorPartHeightValidator = new IntValueValidator(1, 100);
	private static final BigDecimalValueValidator scrollSpeedValidator = new BigDecimalValueValidator(
			new BigDecimal("0.1"), new BigDecimal("2.0"), false);

	private Theme theme = GraphicalConfig.theme;

	private int inputSize = GraphicalConfig.inputSize;
	private int noteHeight = GraphicalConfig.noteHeight;
	private int noteWidth = GraphicalConfig.noteWidth;
	private int chartTextHeight = GraphicalConfig.chartTextHeight;
	private int timingHeight = GraphicalConfig.timingHeight;
	private int chartMapHeightMultiplier = GraphicalConfig.chartMapHeightMultiplier;
	private boolean showChordBoxes = GraphicalConfig.showChordBoxes;
	private BigDecimal previewScrollSpeed = BigDecimal.valueOf(GraphicalConfig.previewWindowScrollSpeed);

	private FieldWithLabel<CharterSelect<Theme>> themeField;
	private final List<JButton> sizeChangeButtons = new ArrayList<>();
	private FieldWithLabel<TextInputWithValidation> inputSizeField;
	private FieldWithLabel<TextInputWithValidation> noteHeightField;
	private FieldWithLabel<TextInputWithValidation> noteWidthField;
	private FieldWithLabel<TextInputWithValidation> chartTextHeightField;
	private FieldWithLabel<TextInputWithValidation> timingHeightField;
	private FieldWithLabel<TextInputWithValidation> chartMapHeightMultiplierField;
	private FieldWithLabel<JCheckBox> showChordBoxesField;
	private FieldWithLabel<TextInputWithValidation> previewScrollSpeedField;

	@Override
	public Label label() {
		return Label.PAGE_THEME;
	}

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addThemePicker(panel, position);
		position.newRow();

		for (int i = 0; i < sizeLabels.length; i++) {
			addSizeButton(panel, position, i);
		}
		position.newRow();

		addInputSizeField(panel, position);
		position.newRow();

		addNoteHeightInput(panel, position);
		addNoteWidthInput(panel, position);
		position.newRow();

		addChartTextHeightField(panel, position);
		position.newRow();

		addTimingHeightFieldField(panel, position);
		position.newRow();

		addChartMapHeightMultiplierInput(panel, position);
		position.newRow();

		addShowChordBoxesInput(panel, position);
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

		themeField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_THEME, inputSize * 3, inputSize * 15 / 2, inputSize,
				input, LabelPosition.LEFT);
		panel.add(themeField, position);
	}

	private void onNoteHeightChange(final int newHeight) {
		noteHeight = newHeight;
		if (theme == Theme.MODERN) {
			noteWidthField.field.setText(noteHeight + "");
		}
	}

	private void setSize(final int size) {
		inputSizeField.field.setText(sizesNotes[size] + "");
		noteHeightField.field.setText(sizesNotes[size] + "");
		noteWidthField.field.setText(sizesNotes[size] + "");
		chartTextHeightField.field.setText(sizesText[size] + "");
		timingHeightField.field.setText(sizesTiming[size] + "");
		chartMapHeightMultiplierField.field.setText(sizesChartMap[size] + "");
	}

	private void addSizeButton(final RowedPanel panel, final RowedPosition position, final int size) {
		final JButton button = new JButton(sizeLabels[size].label());
		button.addActionListener(e -> setSize(size));
		setDefaultFontSize(button);

		panel.addWithSettingSize(button, position, inputSize * 3 / 2);
		sizeChangeButtons.add(button);
	}

	private FieldWithLabel<TextInputWithValidation> addSizeField(final RowedPanel panel, final RowedPosition position,
			final Label label, final int value, final IntValueValidator validator, final IntConsumer onChange) {
		final TextInputWithValidation input = generateForInt(value, inputSize, validator, onChange, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		final FieldWithLabel<TextInputWithValidation> field = new FieldWithLabel<>(label, inputSize * 6,
				inputSize * 3 / 2, inputSize, input, LabelPosition.LEFT_CLOSE);
		panel.add(field, position);

		return field;
	}

	private void addInputSizeField(final RowedPanel panel, final RowedPosition position) {
		inputSizeField = addSizeField(panel, position, Label.INPUT_SIZE, inputSize, new IntValueValidator(5, 200),
				v -> inputSize = v);
	}

	private void addNoteHeightInput(final RowedPanel panel, final RowedPosition position) {
		noteHeightField = addSizeField(panel, position, Label.GRAPHIC_CONFIG_NOTE_HEIGHT, noteHeight,
				editorPartHeightValidator, this::onNoteHeightChange);
	}

	private void addNoteWidthInput(final RowedPanel panel, final RowedPosition position) {
		noteWidthField = addSizeField(panel, position, Label.GRAPHIC_CONFIG_NOTE_WIDTH, noteWidth,
				editorPartHeightValidator, v -> noteWidth = v);
	}

	private void addChartTextHeightField(final RowedPanel panel, final RowedPosition position) {
		chartTextHeightField = addSizeField(panel, position, Label.CHART_TEXT_HEIGHT, chartTextHeight,
				new IntValueValidator(5, 200), v -> chartTextHeight = v);
	}

	private void addTimingHeightFieldField(final RowedPanel panel, final RowedPosition position) {
		timingHeightField = addSizeField(panel, position, Label.GRAPHIC_CONFIG_TIMING_HEIGHT, timingHeight,
				new IntValueValidator(1, 200), v -> timingHeight = v);
	}

	private void addChartMapHeightMultiplierInput(final RowedPanel panel, final RowedPosition position) {
		chartMapHeightMultiplierField = addSizeField(panel, position, Label.GRAPHIC_CONFIG_CHART_MAP_HEIGHT_MULTIPLIER,
				chartMapHeightMultiplier, new IntValueValidator(1, 20), v -> chartMapHeightMultiplier = v);
	}

	private void addShowChordBoxesInput(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.setSelected(showChordBoxes);
		input.addActionListener(e -> showChordBoxes = input.isSelected());

		showChordBoxesField = new FieldWithLabel<>(Label.SHOW_CHORD_BOXES, inputSize * 6, inputSize, inputSize, input,
				LabelPosition.LEFT_CLOSE);
		panel.add(showChordBoxesField, position);
	}

	private void addScrollSpeedFieldField(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForBigDecimal(previewScrollSpeed, inputSize, //
				scrollSpeedValidator, i -> previewScrollSpeed = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);
		input.addKeyListener(numberFilter);

		previewScrollSpeedField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_PREVIEW_SCROLL_SPEED, inputSize * 6,
				inputSize * 3 / 2, inputSize, input, LabelPosition.LEFT_CLOSE);
		panel.add(previewScrollSpeedField, position);
	}

	@Override
	public void setVisible(final boolean visibility) {
		themeField.setVisible(visibility);
		sizeChangeButtons.forEach(b -> b.setVisible(visibility));
		inputSizeField.setVisible(visibility);
		noteHeightField.setVisible(visibility);
		noteWidthField.setVisible(visibility && theme != Theme.MODERN);
		chartTextHeightField.setVisible(visibility);
		timingHeightField.setVisible(visibility);
		chartMapHeightMultiplierField.setVisible(visibility);
		showChordBoxesField.setVisible(visibility);
		previewScrollSpeedField.setVisible(visibility);
	}

	public void save() {
		GraphicalConfig.theme = theme;
		GraphicalConfig.inputSize = inputSize;
		GraphicalConfig.noteHeight = noteHeight;
		GraphicalConfig.noteWidth = noteWidth;
		GraphicalConfig.chartTextHeight = chartTextHeight;
		GraphicalConfig.timingHeight = timingHeight;
		GraphicalConfig.chartMapHeightMultiplier = chartMapHeightMultiplier;
		GraphicalConfig.showChordBoxes = showChordBoxes;
		GraphicalConfig.previewWindowScrollSpeed = previewScrollSpeed.doubleValue();
	}

}
