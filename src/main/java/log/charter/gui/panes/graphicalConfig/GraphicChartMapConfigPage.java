package log.charter.gui.panes.graphicalConfig;

import static log.charter.gui.components.simple.TextInputWithValidation.generateForInt;
import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import javax.swing.JTextField;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.utils.RowedPosition;
import log.charter.gui.components.utils.validators.IntValueValidator;

public class GraphicChartMapConfigPage implements Page {
	private int chartMapHeightMultiplier = GraphicalConfig.chartMapHeightMultiplier;

	private FieldWithLabel<TextInputWithValidation> chartMapHeightMultiplierField;

	@Override
	public Label label() {
		return Label.GRAPHIC_CONFIG_CHART_MAP_PAGE;
	}

	@Override
	public void init(final RowedPanel panel, final RowedPosition position) {
		addChartMapHeightMultiplierInput(panel, position);

		hide();
	}

	private void addChartMapHeightMultiplierInput(final RowedPanel panel, final RowedPosition position) {
		final TextInputWithValidation noteHeightInput = generateForInt(chartMapHeightMultiplier, 20, //
				new IntValueValidator(1, 20), v -> chartMapHeightMultiplier = v, false);
		noteHeightInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(noteHeightInput);

		chartMapHeightMultiplierField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_CHART_MAP_HEIGHT_MULTIPLIER, 90, 30,
				20, noteHeightInput, LabelPosition.LEFT);
		panel.add(chartMapHeightMultiplierField, position);
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
