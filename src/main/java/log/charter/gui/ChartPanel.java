package log.charter.gui;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.editAreaHeight;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.managers.CharterContext;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.gui.chartPanelDrawers.ArrangementDrawer;
import log.charter.gui.chartPanelDrawers.common.BackgroundDrawer;
import log.charter.gui.chartPanelDrawers.common.MarkerDrawer;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseHandler;

public class ChartPanel extends JComponent implements Initiable {
	private static final long serialVersionUID = -3439446235287039031L;

	private CharterContext charterContext;
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private KeyboardHandler keyboardHandler;
	private MouseHandler mouseHandler;

	private final ArrangementDrawer arrangementDrawer = new ArrangementDrawer();
	private final BackgroundDrawer backgroundDrawer = new BackgroundDrawer();
	private final MarkerDrawer markerDrawer = new MarkerDrawer();

	public ChartPanel() {
		super();

		setSize(getWidth(), editAreaHeight);
	}

	@Override
	public void init() {
		charterContext.initObject(arrangementDrawer);
		charterContext.initObject(backgroundDrawer);
		charterContext.initObject(markerDrawer);

		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		addMouseWheelListener(mouseHandler);
		addKeyListener(keyboardHandler);

		setDoubleBuffered(true);
		setFocusable(true);
	}

	private void paintComponent2D(final Graphics2D g) {
		final int time = chartTimeHandler.time();

		backgroundDrawer.draw(g, time);

		if (chartData.isEmpty) {
			return;
		}

		arrangementDrawer.draw(g, time);
		markerDrawer.draw(g);
	}

	@Override
	public void paintComponent(final Graphics g) {
		if (g instanceof Graphics2D) {
			paintComponent2D((Graphics2D) g);
		}
	}
}
