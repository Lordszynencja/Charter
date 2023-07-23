package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.config.Config;
import log.charter.data.config.GridType;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.Pair;

public class GridPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 55;
		sizes.width = 250;

		return sizes;
	}

	private final ChartToolbar chartToolbar;

	private int gridSize = Config.gridSize;
	private GridType gridType = Config.gridType;

	public GridPane(final CharterFrame frame, final ChartToolbar chartToolbar) {
		super(frame, Label.GRID_PANE, 4, getSizes());

		this.chartToolbar = chartToolbar;

		final ArrayList2<Pair<GridType, Label>> gridTypesToPick = new ArrayList2<>(//
//				new Pair<>(GridType.MEASURE, Label.GRID_PANE_MEASURE_TYPE), //
				new Pair<>(GridType.BEAT, Label.GRID_PANE_BEAT_TYPE), //
				new Pair<>(GridType.NOTE, Label.GRID_PANE_NOTE_TYPE));

		int row = 0;
		addIntegerConfigValue(row++, 20, 70, Label.GRID_PANE_GRID_SIZE, gridSize, 50,
				createIntValidator(1, 1024, false), val -> gridSize = Integer.valueOf(val), false);
		addSelectTextOnFocus((JTextField) components.getLast());
		addConfigRadioButtons(row++, 10, 70, gridType, val -> gridType = val, gridTypesToPick);

		row++;
		addDefaultFinish(row, this::saveAndExit);
	}

	private void saveAndExit() {
		Config.gridSize = gridSize;
		Config.gridType = gridType;
		Config.markChanged();

		chartToolbar.updateValues();
	}
}
