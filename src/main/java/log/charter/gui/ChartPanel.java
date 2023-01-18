package log.charter.gui;

import java.awt.Graphics;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.chartPanelDrawers.ArrangementDrawer;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BackgroundDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.HighlightDrawer;
import log.charter.gui.chartPanelDrawers.common.MarkerDrawer;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseHandler;

public class ChartPanel extends JComponent {
	private static final long serialVersionUID = -3439446235287039031L;

	private ChartData data;

	private final ArrangementDrawer arrangementDrawer = new ArrangementDrawer();
	private final BackgroundDrawer backgroundDrawer = new BackgroundDrawer();
	private final HighlightDrawer highlightDrawer = new HighlightDrawer();
	private final MarkerDrawer markerDrawer = new MarkerDrawer();

	public ChartPanel() {
		super();
	}

	public void init(final AudioDrawer audioDrawer, final BeatsDrawer beatsDrawer, final ChartData data,
			final HighlightManager highlightManager, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final MouseHandler mouseHandler, final SelectionManager selectionManager) {
		this.data = data;

		backgroundDrawer.init(data, this);
		arrangementDrawer.init(audioDrawer, beatsDrawer, this, data, keyboardHandler, modeManager, selectionManager);
		highlightDrawer.init(data, highlightManager, modeManager, mouseHandler, mouseButtonPressReleaseHandler,
				selectionManager);
		markerDrawer.init(data);

		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addMouseWheelListener(mouseHandler);
		addKeyListener(keyboardHandler);

		setDoubleBuffered(true);
		setFocusable(true);
	}

	@Override
	public void paintComponent(final Graphics g) {
		backgroundDrawer.draw(g);

		if (data.isEmpty) {
			return;
		}

		arrangementDrawer.draw(g);
		highlightDrawer.draw(g);
		markerDrawer.draw(g);
	}
}
