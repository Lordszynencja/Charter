package log.charter.gui.panes.programConfig;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;

import javax.swing.JCheckBox;

import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class ProgramDisplayConfigPage implements Page {

	private int markerOffset = Config.markerOffset;
	private boolean invertStrings = Config.invertStrings;
	private boolean invertStrings3D = Config.invertStrings3D;
	private boolean leftHanded = Config.leftHanded;
	private boolean showChordIds = Config.showChordIds;
	private boolean showGrid = Config.showGrid;
	private int FPS = Config.FPS;

	private FieldWithLabel<TextInputWithValidation> markerOffsetField;
	private FieldWithLabel<JCheckBox> invertStringsField;
	private FieldWithLabel<JCheckBox> invertStrings3DField;
	private FieldWithLabel<JCheckBox> leftHandedField;
	private FieldWithLabel<JCheckBox> showChordIdsField;
	private FieldWithLabel<JCheckBox> showGridField;
	private FieldWithLabel<TextInputWithValidation> FPSField;

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addMarkerOffset(panel, position);
		position.newRow();

		addInvertStrings(panel, position);
		position.addX(20);
		addInvertStrings3D(panel, position);
		position.newRow();

		addLeftHanded(panel, position);
		position.newRow();

		addShowChordIds(panel, position);
		position.newRow();

		addShowGrid(panel, position);
		position.newRow();

		addFPS(panel, position);
		position.newRow();
	}

	private void addMarkerOffset(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(markerOffset, 50, new IntValueValidator(0, 999_999),
				v -> markerOffset = v, false);

		markerOffsetField = new FieldWithLabel<>(Label.MARKER_POSITION_PX, 125, 50, 20, input, LabelPosition.LEFT);
		panel.add(markerOffsetField, position);
	}

	private void addInvertStrings(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> invertStrings = input.isSelected());
		input.setSelected(invertStrings);

		invertStringsField = new FieldWithLabel<>(Label.INVERT_STRINGS, 100, 20, 20, input, LabelPosition.LEFT);
		panel.add(invertStringsField, position);
	}

	private void addInvertStrings3D(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> invertStrings3D = input.isSelected());
		input.setSelected(invertStrings3D);

		invertStrings3DField = new FieldWithLabel<>(Label.INVERT_STRINGS_IN_PREVIEW, 125, 20, 20, input,
				LabelPosition.LEFT);
		panel.add(invertStrings3DField, position);
	}

	private void addLeftHanded(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> leftHanded = input.isSelected());
		input.setSelected(leftHanded);

		leftHandedField = new FieldWithLabel<>(Label.LEFT_HANDED, 100, 20, 20, input, LabelPosition.LEFT);
		panel.add(leftHandedField, position);
	}

	private void addShowChordIds(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> showChordIds = input.isSelected());
		input.setSelected(showChordIds);

		showChordIdsField = new FieldWithLabel<>(Label.SHOW_CHORD_IDS, 100, 20, 20, input, LabelPosition.LEFT);
		panel.add(showChordIdsField, position);
	}

	private void addShowGrid(final RowedPanel panel, final RowedPosition position) {
		final JCheckBox input = new JCheckBox();
		input.addActionListener(a -> showGrid = input.isSelected());
		input.setSelected(showGrid);

		showGridField = new FieldWithLabel<>(Label.SHOW_GRID, 100, 20, 20, input, LabelPosition.LEFT);
		panel.add(showGridField, position);
	}

	private void addFPS(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation input = generateForInt(FPS, 50, new IntValueValidator(1, 1000), v -> FPS = v,
				false);

		FPSField = new FieldWithLabel<>(Label.FPS, 125, 50, 20, input, LabelPosition.LEFT);
		panel.add(FPSField, position);
	}

	@Override
	public Label label() {
		return Label.CONFIG_DISPLAY;
	}

	@Override
	public void show() {
		markerOffsetField.setVisible(true);
		invertStringsField.setVisible(true);
		invertStrings3DField.setVisible(true);
		leftHandedField.setVisible(true);
		showChordIdsField.setVisible(true);
		showGridField.setVisible(true);
		FPSField.setVisible(true);
	}

	@Override
	public void hide() {
		markerOffsetField.setVisible(false);
		invertStringsField.setVisible(false);
		invertStrings3DField.setVisible(false);
		leftHandedField.setVisible(false);
		showChordIdsField.setVisible(false);
		showGridField.setVisible(false);
		FPSField.setVisible(false);
	}

	public void save() {
		Config.markerOffset = markerOffset;
		Config.invertStrings = invertStrings;
		Config.invertStrings3D = invertStrings3D;
		Config.leftHanded = leftHanded;
		Config.showChordIds = showChordIds;
		Config.showGrid = showGrid;
		Config.FPS = FPS;
	}
}
