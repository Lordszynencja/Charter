package log.charter.gui.panes.colorConfig;

import static log.charter.gui.components.utils.ComponentUtils.addComponentCenteringOnResize;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.CharterScrollPane;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.data.PaneSizesBuilder;
import log.charter.gui.components.simple.CharterSelect;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.ComponentUtils.ComponentWithOffset;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.util.FileUtils;

public class ColorConfigPane extends ParamsPane {
	private static final long serialVersionUID = 1L;

	private final CharterSelect<String> fileSelect;
	private final CharterScrollPane panel;
	private List<ColorPicker> colorPickers;

	public ColorConfigPane(final CharterFrame frame) {
		super(frame, Label.GRAPHIC_CONFIG_PANE, 400);

		fileSelect = makeFileSelect();
		panel = makePanel();

		this.addDefaultFinish(21, this::onSave, false);

		addComponentCenteringOnResize(this, //
				new ComponentWithOffset(fileSelect, 0), //
				new ComponentWithOffset(panel, 0), //
				new ComponentWithOffset(getPart(getPartsSize() - 2), 55), //
				new ComponentWithOffset(getPart(getPartsSize() - 1), -55));

		setVisible(true);
	}

	private void changeSet(final String newName) {
		final Map<ColorLabel, Color> colors = ChartPanelColors.readColors(newName);

		colorPickers.forEach(picker -> picker.color(colors.get(picker.colorLabel)));
	}

	private CharterSelect<String> makeFileSelect() {
		final Stream<String> names = FileUtils
				.listFiles(FileUtils.colorSetsFolder, file -> file.getName().endsWith(".txt"))//
				.map(name -> name.substring(0, name.length() - 4));

		final CharterSelect<String> select = new CharterSelect<>(names, GraphicalConfig.colorSet, this::changeSet);

		add(select, 20, 0, 200, 20);

		return select;
	}

	private CharterScrollPane makePanel() {
		final int valuesAmount = ColorLabel.values().length;
		final int horizontalSpace = 20;
		final int width = sizes.width - 2 * horizontalSpace;
		final int labelWidth = 300;
		final int inputWidth = 20;

		final RowedPanel panel = new RowedPanel(new PaneSizesBuilder(width).build());

		colorPickers = new ArrayList<>(valuesAmount);
		final RowedPosition position = new RowedPosition(10, 10, sizes.rowDistance);
		boolean even = false;
		for (final ColorLabel colorLabel : ColorLabel.values()) {
			final ColorPicker colorPicker = new ColorPicker(colorLabel);
			colorPickers.add(colorPicker);

			final FieldWithLabel<ColorPicker> field = new FieldWithLabel<>(colorLabel.label(), labelWidth, inputWidth,
					20, colorPicker, LabelPosition.LEFT);
			if (even) {
				field.backgroundColor = ColorLabel.BASE_BG_3;
			}

			panel.addWithSpace(field, position, field.getWidth(), 0);
			position.newRow();
			even = !even;
		}

		final CharterScrollPane scrollPane = new CharterScrollPane(panel);
		this.add(scrollPane, horizontalSpace, getY(1), width, getY(19) - getY(0));

		return scrollPane;
	}

	private void onSave() {
		GraphicalConfig.colorSet = fileSelect.getSelectedItem();
		colorPickers.forEach(colorPicker -> colorPicker.colorLabel.setColor(colorPicker.color()));
		ChartPanelColors.save();
	}

}
