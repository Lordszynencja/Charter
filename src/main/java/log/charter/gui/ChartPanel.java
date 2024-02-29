package log.charter.gui;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.editAreaHeight;

import java.awt.Graphics;

import javax.swing.JComponent;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.chartPanelDrawers.ArrangementDrawer;
import log.charter.gui.chartPanelDrawers.common.BackgroundDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.MarkerDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.mouseAndKeyboard.MouseHandler;

public class ChartPanel extends JComponent {
	private static final long serialVersionUID = -3439446235287039031L;

	private ChartData data;
	private HighlightManager highlightManager;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private MouseHandler mouseHandler;
	private SelectionManager selectionManager;

	private final ArrangementDrawer arrangementDrawer = new ArrangementDrawer();
	private final BackgroundDrawer backgroundDrawer = new BackgroundDrawer();
	private final LyricLinesDrawer lyricLinesDrawer = new LyricLinesDrawer();
	private final MarkerDrawer markerDrawer = new MarkerDrawer();

	public ChartPanel() {
		super();

		setSize(getWidth(), editAreaHeight);
	}

	public void init(final BeatsDrawer beatsDrawer, final ChartData data, final HighlightManager highlightManager,
			final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler, final MouseHandler mouseHandler,
			final SelectionManager selectionManager, final WaveFormDrawer waveFormDrawer) {
		this.data = data;
		this.highlightManager = highlightManager;
		this.modeManager = modeManager;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.mouseHandler = mouseHandler;
		this.selectionManager = selectionManager;

		backgroundDrawer.init(data, this);
		arrangementDrawer.init(beatsDrawer, this, data, keyboardHandler, lyricLinesDrawer, modeManager,
				selectionManager, waveFormDrawer);
		lyricLinesDrawer.init(data);
		markerDrawer.init();

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

		final HighlightData highlightData = HighlightData.getCurrentHighlight(data, highlightManager, modeManager,
				mouseButtonPressReleaseHandler, mouseHandler, selectionManager);

		arrangementDrawer.draw(g, highlightData);
		markerDrawer.draw(g);
	}
}
