package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.GridType;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;

public class GridPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 55;
		sizes.width = 250;

		return sizes;
	}

	private int gridSize = Config.gridSize;
	private GridType gridType = Config.gridType;

	public GridPane(final CharterFrame frame) {
		super(frame, Label.GRID_PANE, 4, getSizes());

		int row = 0;
		addIntegerConfigValue(row++, 20, 70, Label.GRID_PANE_GRID_SIZE, gridSize, 50,
				createIntValidator(1, 1024, false), val -> gridSize = Integer.valueOf(val), false);
		addSelectTextOnFocus((JTextField) components.getLast());
		addConfigRadioButtons(row++, 10, 70, gridType.ordinal(), val -> gridType = GridType.values()[val], //
				// Label.GRID_PANE_MEASURE_TYPE, //
				Label.GRID_PANE_BEAT_TYPE, //
				Label.GRID_PANE_NOTE_TYPE);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void saveAndExit() {
		Config.gridSize = gridSize;
		Config.gridType = gridType;
		Config.markChanged();
	}
}
