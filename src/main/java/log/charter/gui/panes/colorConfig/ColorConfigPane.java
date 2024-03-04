package log.charter.gui.panes.colorConfig;

import static log.charter.gui.components.utils.ComponentUtils.addComponentCenteringOnResize;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.data.PaneSizesBuilder;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.utils.ComponentUtils.ComponentWithOffset;
import log.charter.gui.components.utils.RowedPosition;

public class ColorConfigPane extends ParamsPane {
	private static final long serialVersionUID = 1L;

	private final Component fileSelect;
	private final JScrollPane panel;
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

	private Component makeFileSelect() {
		final Component fileSelect = new JLabel("color set select", JLabel.CENTER);
		add(fileSelect, 20, 0, 200, 20);

		return fileSelect;
	}

	private JScrollPane makePanel() {
		final int valuesAmount = ColorLabel.values().length;
		final int horizontalSpace = 20;
		final int width = sizes.width - 2 * horizontalSpace;
		final int labelWidth = 300;
		final int inputWidth = 20;

		final RowedPanel panel = new RowedPanel(new PaneSizesBuilder(width).build(), valuesAmount);

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

			panel.add(field, position, field.getWidth() + 10);
			position.newRow();
			even = !even;
		}

		final JScrollPane scrollPane = new JScrollPane(panel);
		this.add(scrollPane, horizontalSpace, getY(1), width, getY(19) - getY(0));

		return scrollPane;
	}

	private void onSave() {
		colorPickers.forEach(colorPicker -> colorPicker.colorLabel.setColor(colorPicker.color()));
		ChartPanelColors.saveColors();
	}

}
