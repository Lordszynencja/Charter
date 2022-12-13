package log.charter.gui.panes;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import log.charter.data.ChartData;
import log.charter.gui.CharterFrame;

public class GridPane extends ParamsPane {
	private static final long serialVersionUID = -4754359602173894487L;

	private int gridSize;
	private boolean useGrid;

	private final ChartData data;

	public GridPane(final CharterFrame frame, final ChartData data) {
		super(frame, "Grid options", 5);
		this.data = data;

		gridSize = data.gridSize;
		useGrid = data.useGrid;

		addConfigValue(0, "Grid size", gridSize, 50, createIntValidator(1, 1024, false),
				val -> gridSize = Integer.valueOf(val), false);
		addConfigCheckbox(1, "Use grid", useGrid, val -> useGrid = val);

		addButtons(4, e -> saveAndExit());
		getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().registerKeyboardAction(e -> saveAndExit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		validate();
		setVisible(true);
	}

	private void saveAndExit() {
		data.gridSize = gridSize;
		data.useGrid = useGrid;

		dispose();
	}
}
