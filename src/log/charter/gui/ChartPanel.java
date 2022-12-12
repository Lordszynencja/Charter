package log.charter.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import log.charter.data.ChartData;
import log.charter.gui.chartPanelDrawers.common.BackgroundDrawer;
import log.charter.gui.chartPanelDrawers.common.HighlightDrawer;
import log.charter.gui.chartPanelDrawers.common.MarkerDrawer;
import log.charter.gui.chartPanelDrawers.instruments.ArrangementDrawer;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.ChartPanelMouseListener;

public class ChartPanel extends JPanel {
	private static final long serialVersionUID = -3439446235287039031L;

	private ChartData data;

	private final ArrangementDrawer arrangementDrawer = new ArrangementDrawer();
	private final BackgroundDrawer backgroundDrawer = new BackgroundDrawer();
	private final HighlightDrawer highlightDrawer = new HighlightDrawer();
	private final MarkerDrawer markerDrawer = new MarkerDrawer();

	public ChartPanel() {
		super();
	}

	public void init(final ChartPanelMouseListener chartPanelMouseListener, final AudioHandler audioHandler,
			final ChartData data, final ChartKeyboardHandler chartKeyboardHandler,
			final HighlightManager highlightManager, final SelectionManager selectionManager) {
		this.data = data;

		backgroundDrawer.init(data, this);
		arrangementDrawer.init(this, data, selectionManager);
		highlightDrawer.init(chartPanelMouseListener, data, highlightManager);

		addMouseListener(chartPanelMouseListener);
		addMouseMotionListener(chartPanelMouseListener);
		addMouseWheelListener(chartPanelMouseListener);

		setDoubleBuffered(true);
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
