package log.charter.gui.panes.colorConfig;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import log.charter.data.config.Localization.Label;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.containers.ParamsPane;
import log.charter.gui.components.containers.ScrollableRowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.util.CollectionUtils.Pair;

public class ColorConfigPane extends ParamsPane {
	private static final long serialVersionUID = 1L;

	private static final int width = 400;
	private static final int labelWidth = 300;

	private final List<Pair<ColorLabel, Color>> colors = new ArrayList<>();

	private final Component fileSelect;
	private final ScrollableRowedPanel panel;
	private List<ColorPicker> colorPickers;

	public ColorConfigPane(final CharterFrame frame) {
		super(frame, Label.GRAPHIC_CONFIG_PANE, 400);

		fileSelect = makeFileSelect();
		panel = makePanel();

		this.addDefaultFinish(20, this::onSave);
	}

	private Component makeFileSelect() {
		final Component fileSelect = new JLabel("color set select", JLabel.CENTER);
		add(fileSelect, 20, 0, 200, 20);

		return fileSelect;
	}

	private ScrollableRowedPanel makePanel() {
		final int valuesAmount = ColorLabel.values().length;
		final ScrollableRowedPanel panel = new ScrollableRowedPanel(width, valuesAmount);

		colorPickers = new ArrayList<>(valuesAmount);
		int row = 0;
		for (final ColorLabel colorLabel : ColorLabel.values()) {
			final ColorPicker colorPicker = new ColorPicker(colorLabel);
			colorPickers.add(colorPicker);

			final FieldWithLabel<ColorPicker> field = new FieldWithLabel<>(colorLabel.label(), labelWidth, 20, 20,
					colorPicker, LabelPosition.LEFT);
			panel.add(field, 20, row++, 360, 20);
			colors.add(new Pair<>(colorLabel, colorLabel.color()));
		}

		this.add(panel, 20, getY(1), sizes.width - 40, getY(19) - getY(0));

		return panel;
	}

	private void resize() {
		final int middleX = getWidth() / 2;

	}

	private void onSave() {
		colors.forEach(pair -> pair.a.setColor(pair.b));
		ChartPanelColors.saveColors();
	}

}
