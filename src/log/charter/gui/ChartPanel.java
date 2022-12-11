package log.charter.gui;

import java.awt.Graphics;

import javax.swing.JPanel;

import log.charter.data.ChartData;
import log.charter.data.Config;
import log.charter.data.EditMode;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.HighlightDrawer;
import log.charter.gui.chartPanelDrawers.instruments.ArrangementDrawer;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.CharterFrameMouseListener;
import log.charter.gui.handlers.CharterFrameMouseMotionListener;

public class ChartPanel extends JPanel {
	private static final long serialVersionUID = -3439446235287039031L;

	public static final int sectionNamesY = 10;
	public static final int lyricLinesY = sectionNamesY + 20;
	public static final int textY = lyricLinesY + 10;
	public static final int beatTextY = textY + 20;
	public static final int beatSizeTextY = beatTextY + 15;
	public static final int lanesTop = beatSizeTextY + 5;
	public static final int lanesBottom = lanesTop + 250;
	public static final int handShapesY = lanesBottom + 30;
	public static final int HEIGHT = handShapesY + 20;

	public static final int lanesHeight = lanesBottom - lanesTop;

	private static int applyInvertion(final int lane, final int lanesNo) {
		return Config.invertStrings ? lane : lanesNo - lane - 1;
	}

	public static int getLaneY(final int lane, final int lanesNo) {
		return lanesTop + (int) (lanesHeight * (applyInvertion(lane, lanesNo) + 0.5) / lanesNo);
	}

	public static int clamp(final double y, final int lanesNo) {
		return getLaneY(yToLane(y, lanesNo), lanesNo);
	}

	public static boolean isInLanes(final int y) {
		return (y >= lanesTop) && (y <= lanesBottom);
	}

	public static boolean isInTempos(final int y) {
		return (y >= beatTextY) && (y < lanesTop);
	}

	public static int yToLane(final double y, final int lanesNo) {
		return applyInvertion((int) ((y - lanesTop) * lanesNo / lanesHeight), lanesNo);
	}

	private boolean initiated = false;

	private ChartData data;

	private final HighlightDrawer highlightDrawer = new HighlightDrawer();
	private final ArrangementDrawer arrangementDrawer = new ArrangementDrawer();

	public ChartPanel() {
		super();
	}

	public void init(final AudioHandler audioHandler, final ChartData data,
			final ChartKeyboardHandler chartEventsHandler, final HighlightManager highlightManager,
			final SelectionManager selectionManager) {
		this.data = data;

		arrangementDrawer.init(this, data, highlightDrawer, selectionManager);
		highlightDrawer.init(data, highlightManager);

		addMouseListener(new CharterFrameMouseListener(audioHandler, data));
		addMouseMotionListener(new CharterFrameMouseMotionListener(data));

		initiated = true;
	}

	private void drawMarker(final Graphics g) {
		g.setColor(ChartPanelColors.get(ColorLabel.MARKER));
		g.drawLine(Config.markerOffset, lanesTop - 5, Config.markerOffset, lanesBottom);
	}

	private void drawMouseDrag(final Graphics g) {
		if (data.editMode == EditMode.GUITAR && data.isNoteAdd) {
			g.setColor(ChartPanelColors.get(ColorLabel.NOTE_ADD_LINE));
			g.drawLine(data.mousePressX, data.mousePressY, data.mx, data.my);
		}
	}

	@Override
	public void paintComponent(final Graphics g) {
		if (!initiated) {
			return;
		}

		g.setColor(ChartPanelColors.get(ColorLabel.BACKGROUND));
		g.fillRect(0, 0, getWidth(), getHeight());
		if (data.isEmpty) {
			return;
		}

		g.setColor(ChartPanelColors.get(ColorLabel.NOTE_BACKGROUND));
		g.fillRect(0, lanesTop, getWidth(), lanesHeight);

		arrangementDrawer.draw(g);

		drawMarker(g);
		drawMouseDrag(g);
	}
}
