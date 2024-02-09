package log.charter.gui.panes.graphicalConfig;

import static java.util.Arrays.asList;
import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.Theme;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.Page;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.gui.components.TextInputWithValidation.IntegerValueValidator;

public class GraphicThemeConfigPage implements Page {
	private static class ThemeHolder {
		public final Theme theme;
		public final Label label;

		public ThemeHolder(final Theme theme, final Label label) {
			this.theme = theme;
			this.label = label;
		}

		@Override
		public String toString() {
			return label.label();
		}
	}

	private Theme theme = GraphicalConfig.theme;

	private int toneChangeHeight = GraphicalConfig.toneChangeHeight;
	private int anchorInfoHeight = GraphicalConfig.anchorInfoHeight;
	private int noteHeight = GraphicalConfig.noteHeight;
	private int noteWidth = GraphicalConfig.noteWidth;
	private int handShapesHeight = GraphicalConfig.handShapesHeight;
	private int timingHeight = GraphicalConfig.timingHeight;

	private FieldWithLabel<JComboBox<ThemeHolder>> themeField;
	private FieldWithLabel<TextInputWithValidation> noteHeightField;
	private FieldWithLabel<TextInputWithValidation> noteWidthField;
	private FieldWithLabel<TextInputWithValidation> toneChangeHeightField;
	private FieldWithLabel<TextInputWithValidation> anchorInfoHeightField;
	private FieldWithLabel<TextInputWithValidation> handShapesHeightField;
	private FieldWithLabel<TextInputWithValidation> timingHeightField;

	public void init(final GraphicConfigPane parent, int row) {
		addThemePicker(parent, row++);

		addToneChangeHeightField(parent, row);
		addAnchorInfoHeightField(parent, row++);

		addNoteHeightInput(parent, row);
		addNoteWidthInput(parent, row++);

		addHandShapesHeightFieldField(parent, row);
		addTimingHeightFieldField(parent, row++);

		hide();
	}

	private void addThemePicker(final GraphicConfigPane parent, final int row) {
		final Vector<ThemeHolder> themes = new Vector<>(asList(//
				new ThemeHolder(Theme.MODERN, Label.CONFIG_THEME_MODERN), //
				new ThemeHolder(Theme.ROCKSMITH, Label.CONFIG_THEME_ROCKSMITH), //
				new ThemeHolder(Theme.BASIC, Label.CONFIG_THEME_BASIC)));

		final JComboBox<ThemeHolder> themeSelect = new JComboBox<>(themes);
		for (int i = 0; i < themes.size(); i++) {
			if (themes.get(i).theme == theme) {
				themeSelect.setSelectedIndex(i);
				break;
			}
		}
		themeSelect.addActionListener(e -> onThemeChange(((ThemeHolder) themeSelect.getSelectedItem()).theme));

		themeField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_THEME, 60, 150, 20, themeSelect, LabelPosition.LEFT);
		themeField.setLocation(10, parent.getY(row));
		parent.add(themeField);
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

	private void addToneChangeHeightField(final GraphicConfigPane parent, final int row) {
		final TextInputWithValidation input = new TextInputWithValidation(toneChangeHeight, 20,
				new IntegerValueValidator(1, 100, false), i -> toneChangeHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		toneChangeHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_TONE_CHANGE_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		toneChangeHeightField.setLocation(10, parent.getY(row));
		parent.add(toneChangeHeightField);
	}

	private void addAnchorInfoHeightField(final GraphicConfigPane parent, final int row) {
		final TextInputWithValidation input = new TextInputWithValidation(anchorInfoHeight, 20,
				new IntegerValueValidator(1, 100, false), i -> anchorInfoHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		anchorInfoHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_ANCHOR_INFO_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		anchorInfoHeightField.setLocation(170, parent.getY(row));
		parent.add(anchorInfoHeightField);
	}

	private void addNoteHeightInput(final GraphicConfigPane parent, final int row) {
		final TextInputWithValidation input = new TextInputWithValidation(noteHeight, 20,
				new IntegerValueValidator(1, 100, false), this::onNoteHeightChange, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		noteHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_NOTE_HEIGHT, 100, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		noteHeightField.setLocation(10, parent.getY(row));
		parent.add(noteHeightField);
	}

	private void onNoteHeightChange(final int newHeight) {
		noteHeight = newHeight;
		if (theme == Theme.MODERN) {
			noteWidthField.field.setText(noteHeight + "");
		}
	}

	private void addNoteWidthInput(final GraphicConfigPane parent, final int row) {
		final TextInputWithValidation input = new TextInputWithValidation(noteWidth, 20,
				new IntegerValueValidator(1, 100, false), i -> noteWidth = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		noteWidthField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_NOTE_WIDTH, 100, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		noteWidthField.setLocation(160, parent.getY(row));
		parent.add(noteWidthField);
	}

	private void addHandShapesHeightFieldField(final GraphicConfigPane parent, final int row) {
		final TextInputWithValidation input = new TextInputWithValidation(handShapesHeight, 20,
				new IntegerValueValidator(1, 100, false), i -> handShapesHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		handShapesHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_HAND_SHAPES_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		handShapesHeightField.setLocation(10, parent.getY(row));
		parent.add(handShapesHeightField);
	}

	private void addTimingHeightFieldField(final GraphicConfigPane parent, final int row) {
		final TextInputWithValidation input = new TextInputWithValidation(timingHeight, 20,
				new IntegerValueValidator(1, 200, false), i -> timingHeight = i, false);
		input.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(input);

		timingHeightField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_TIMING_HEIGHT, 120, 30, 20, input,
				LabelPosition.LEFT_CLOSE);
		timingHeightField.setLocation(170, parent.getY(row));
		parent.add(timingHeightField);
	}

	@Override
	public void show() {
		themeField.setVisible(true);
		toneChangeHeightField.setVisible(true);
		anchorInfoHeightField.setVisible(true);
		noteHeightField.setVisible(true);
		noteWidthField.setVisible(theme != Theme.MODERN);
		handShapesHeightField.setVisible(true);
		timingHeightField.setVisible(true);
	}

	@Override
	public void hide() {
		themeField.setVisible(false);
		toneChangeHeightField.setVisible(false);
		anchorInfoHeightField.setVisible(false);
		noteHeightField.setVisible(false);
		noteWidthField.setVisible(false);
		handShapesHeightField.setVisible(false);
		timingHeightField.setVisible(false);
	}

	public void save() {
		GraphicalConfig.theme = theme;
		GraphicalConfig.toneChangeHeight = toneChangeHeight;
		GraphicalConfig.anchorInfoHeight = anchorInfoHeight;
		GraphicalConfig.noteHeight = noteHeight;
		GraphicalConfig.noteWidth = noteWidth;
		GraphicalConfig.handShapesHeight = handShapesHeight;
		GraphicalConfig.timingHeight = timingHeight;
	}
}
