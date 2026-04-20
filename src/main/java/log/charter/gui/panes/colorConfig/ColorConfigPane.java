package log.charter.gui.panes.colorConfig;

import static log.charter.data.config.GraphicalConfig.inputSize;
import static log.charter.gui.components.utils.ComponentUtils.addComponentCenteringOnResize;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.UIManager;

import log.charter.data.config.ChartPanelColors;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.ColorMap;
import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.RowedDialog;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.containers.SaverWithStatus;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.components.utils.ComponentUtils.ComponentWithOffset;
import log.charter.gui.components.utils.PaneSizesBuilder;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.util.FileUtils;

public class ColorConfigPane extends RowedDialog {
	private static final long serialVersionUID = 1L;

	private final CharterSelect<String> fileSelect;
	private final CharterScrollPane colorsPanel;
	private final Component saveButton;
	private final Component cancelButton;

	private final Map<ColorLabel, ColorPicker> pickersForLabels = new HashMap<>();

	public ColorConfigPane(final CharterFrame frame) {
		super(frame, Label.GRAPHIC_CONFIG_PANE, 0);

		final RowedPosition position = new RowedPosition(inputSize, panel.sizes);
		fileSelect = makeFileSelect(position);
		position.newRows(2);

		colorsPanel = makePanel(position);
		position.newRows(17);

		addDefaultFinish(position.y(), SaverWithStatus.defaultFor(this::onSave), null, false);
		saveButton = panel.getPart(-2);
		cancelButton = panel.getPart(-1);

		addComponentCenteringOnResize(this, //
				new ComponentWithOffset(fileSelect, 0), //
				new ComponentWithOffset(colorsPanel, 0), //
				new ComponentWithOffset(saveButton, inputSize * 11 / 4), //
				new ComponentWithOffset(cancelButton, -inputSize * 11 / 4));

		finishInit();
	}

	private void changeSet(final String newName) {
		final Map<ColorLabel, Color> colors = ColorMap.forSet(newName).colors;
		pickersForLabels.forEach((colorLabel, picker) -> picker.color(colors.get(colorLabel)));
	}

	private CharterSelect<String> makeFileSelect(final RowedPosition position) {
		final Stream<String> names = FileUtils
				.listFiles(FileUtils.colorSetsFolder, file -> file.getName().endsWith(".txt"))//
				.map(name -> name.substring(0, name.length() - 4));

		final CharterSelect<String> select = new CharterSelect<>(names, GraphicalConfig.colorSet, null,
				this::changeSet);

		ComponentUtils.setDefaultFontSize(select);
		panel.addWithSettingSize(select, position, inputSize * 10);

		return select;
	}

	private CharterScrollPane makePanel(final RowedPosition position) {
		final int labelWidth = inputSize * 15;
		final int fieldWidth = labelWidth + inputSize * 5 / 4;

		final RowedPanel colorsPanel = new RowedPanel(new PaneSizesBuilder(labelWidth + inputSize)//
				.rowHeight(inputSize)//
				.rowSpacing(0)//
				.verticalSpace(0).build());

		final RowedPosition panelPosition = new RowedPosition(0, colorsPanel.sizes);
		boolean even = false;
		for (final ColorLabel colorLabel : ColorLabel.values()) {
			final ColorPicker colorPicker = new ColorPicker(colorLabel);
			pickersForLabels.put(colorLabel, colorPicker);

			final FieldWithLabel<ColorPicker> field = new FieldWithLabel<>(colorLabel.label(), labelWidth, inputSize,
					inputSize, colorPicker, LabelPosition.LEFT);
			if (even) {
				field.backgroundColor = ColorLabel.BASE_BG_3;
			}

			colorsPanel.addWithSpace(field, panelPosition, field.getWidth(), 0);
			panelPosition.newRow();
			even = !even;
		}

		final CharterScrollPane scrollPane = new CharterScrollPane(colorsPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);

		final Insets scrollPaneInsets = scrollPane.getInsets();
		final int scrollPaneWidth = fieldWidth + ((Integer) UIManager.get("ScrollBar.width")) + scrollPaneInsets.left
				+ scrollPaneInsets.right;
		final int scrollPaneHeight = colorsPanel.sizes.getHeight(20);
		panel.addWithSettingSize(scrollPane, position, scrollPaneWidth, inputSize, scrollPaneHeight);

		return scrollPane;
	}

	private void onSave() {
		GraphicalConfig.colorSet = fileSelect.getSelectedValue();
		pickersForLabels.forEach((label, picker) -> label.setColor(picker.color()));

		GraphicalConfig.markChanged();
		GraphicalConfig.save();
		ChartPanelColors.save();
	}

}
