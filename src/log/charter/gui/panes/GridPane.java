package log.charter.gui.panes;

import static log.charter.gui.components.TextInputSelectAllOnFocus.addSelectTextOnFocus;
import static log.charter.gui.components.TextInputWithValidation.ValueValidator.createIntValidator;

import javax.swing.JTextField;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.ParamsPane;
import log.charter.song.BeatsMap;

public class GridPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private static PaneSizes getSizes() {
		final PaneSizes sizes = new PaneSizes();
		sizes.labelWidth = 55;
		sizes.width = 250;

		return sizes;
	}

	private int gridSize;
	private boolean useGrid;

	private final BeatsMap beatsMap;

	public GridPane(final CharterFrame frame, final BeatsMap beatsMap) {
		super(frame, Label.GRID_PANE, 4, getSizes());
		this.beatsMap = beatsMap;

		gridSize = beatsMap.gridSize;
		useGrid = beatsMap.useGrid;

		addIntegerConfigValue(0, 20, 0, Label.GRID_PANE_GRID_SIZE, gridSize, 50, createIntValidator(1, 1024, false),
				val -> gridSize = Integer.valueOf(val), false);
		addSelectTextOnFocus((JTextField) components.getLast());
		addConfigCheckbox(1, Label.GRID_PANE_USE_GRID, useGrid, val -> useGrid = val);

		addDefaultFinish(3, this::saveAndExit);
	}

	private void saveAndExit() {
		beatsMap.gridSize = gridSize;
		beatsMap.useGrid = useGrid;
	}
}
