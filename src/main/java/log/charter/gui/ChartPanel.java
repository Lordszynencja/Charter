package log.charter.gui;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.editAreaHeight;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.chartPanelDrawers.ArrangementDrawer;
import log.charter.gui.chartPanelDrawers.common.BackgroundDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.MarkerDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseHandler;

public class ChartPanel extends JComponent {
	private static final long serialVersionUID = -3439446235287039031L;

	private ChartTimeHandler chartTimeHandler;
	private ChartData chartData;

	private final ArrangementDrawer arrangementDrawer = new ArrangementDrawer();
	private final BackgroundDrawer backgroundDrawer = new BackgroundDrawer();
	private final MarkerDrawer markerDrawer = new MarkerDrawer();

	public ChartPanel() {
		super();

		setSize(getWidth(), editAreaHeight);
	}

	public void init(final BeatsDrawer beatsDrawer, final ChartTimeHandler chartTimeHandler, final ChartData chartData,
			final HighlightManager highlightManager, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final MouseHandler mouseHandler, final SelectionManager selectionManager,
			final WaveFormDrawer waveFormDrawer) {
		this.chartTimeHandler = chartTimeHandler;
		this.chartData = chartData;

		backgroundDrawer.init(chartTimeHandler, chartData, this);
		arrangementDrawer.init(beatsDrawer, chartData, this, highlightManager, keyboardHandler, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager, waveFormDrawer);
		markerDrawer.init();

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
