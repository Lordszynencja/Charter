package log.charter.gui.panes.graphicalConfig;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JTextField;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.FieldWithLabel;
import log.charter.gui.components.FieldWithLabel.LabelPosition;
import log.charter.gui.components.Page;
import log.charter.gui.components.TextInputWithValidation;
import log.charter.gui.components.TextInputWithValidation.IntegerValueValidator;

public class GraphicChartMapConfigPage implements Page {
	private int chartMapHeightMultiplier = GraphicalConfig.chartMapHeightMultiplier;

	private FieldWithLabel<TextInputWithValidation> chartMapHeightMultiplierField;

	public void init(final GraphicConfigPane parent, final int row) {
		addChartMapHeightMultiplierInput(parent, row);

		hide();
	}

	private void addChartMapHeightMultiplierInput(final GraphicConfigPane parent, final int row) {
		final TextInputWithValidation noteHeightInput = new TextInputWithValidation(chartMapHeightMultiplier, 20,
				new IntegerValueValidator(1, 20, false), this::onChartMapHeightMultiplierChange, false);
		noteHeightInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(noteHeightInput);
		chartMapHeightMultiplierField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_CHART_MAP_HEIGHT_MULTIPLIER, 90, 30,
				20, noteHeightInput, LabelPosition.LEFT);
		chartMapHeightMultiplierField.setLocation(10, parent.getY(row));
		parent.add(chartMapHeightMultiplierField);
	}

	private void onChartMapHeightMultiplierChange(final int newHeight) {
		chartMapHeightMultiplier = newHeight;
	}

	@Override
	public void show() {
		chartMapHeightMultiplierField.setVisible(true);
	}

	@Override
	public void hide() {
		chartMapHeightMultiplierField.setVisible(false);
	}

	public void save() {
		GraphicalConfig.chartMapHeightMultiplier = chartMapHeightMultiplier;
	}
}
