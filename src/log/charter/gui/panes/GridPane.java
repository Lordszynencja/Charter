package log.charter.gui.panes;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.song.BeatsMap;

public class GridPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private int gridSize;
	private boolean useGrid;

	private final BeatsMap beatsMap;

	public GridPane(final CharterFrame frame, final BeatsMap beatsMap) {
		super(frame, Label.GRID_PANE.label(), 5);
		this.beatsMap = beatsMap;

		gridSize = beatsMap.gridSize;
		useGrid = beatsMap.useGrid;

		addConfigValue(0, Label.GRID_PANE_GRID_SIZE, gridSize, 50, createIntValidator(1, 1024, false),
				val -> gridSize = Integer.valueOf(val), false);
		addConfigCheckbox(1, Label.GRID_PANE_USE_GRID, useGrid, val -> useGrid = val);

		addButtons(4, e -> saveAndExit());
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> saveAndExit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	private void saveAndExit() {
		beatsMap.gridSize = gridSize;
		beatsMap.useGrid = useGrid;

		dispose();
	}
}
