package log.charter.gui.panes.graphicalConfig;

import static log.charter.gui.components.utils.TextInputSelectAllOnFocus.addSelectTextOnFocus;

import java.util.function.IntConsumer;

import javax.swing.JTextField;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.containers.Page;
import log.charter.gui.components.containers.RowedPanel;
import log.charter.gui.components.simple.FieldWithLabel;
import log.charter.gui.components.simple.FieldWithLabel.LabelPosition;
import log.charter.gui.components.simple.TextInputWithValidation;
import log.charter.gui.components.simple.TextInputWithValidation.IntegerValueValidator;
import log.charter.gui.components.utils.RowedPosition;

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
		final TextInputWithValidation noteHeightInput = new TextInputWithValidation(chartMapHeightMultiplier, 20,
				new IntegerValueValidator(1, 20, false), (IntConsumer) this::onChartMapHeightMultiplierChange, false);
		noteHeightInput.setHorizontalAlignment(JTextField.CENTER);
		addSelectTextOnFocus(noteHeightInput);
		chartMapHeightMultiplierField = new FieldWithLabel<>(Label.GRAPHIC_CONFIG_CHART_MAP_HEIGHT_MULTIPLIER, 90, 30,
				20, noteHeightInput, LabelPosition.LEFT);
		panel.add(chartMapHeightMultiplierField, position);
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
