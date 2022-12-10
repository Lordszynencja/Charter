package log.charter.gui;

import static java.util.Arrays.asList;

import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import log.charter.data.ChartData;
import log.charter.data.Config;
import log.charter.data.EditMode;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.Drawer;
import log.charter.gui.chartPanelDrawers.HighlightDrawer;
import log.charter.gui.chartPanelDrawers.SelectedNotesDrawer;
import log.charter.gui.chartPanelDrawers.instruments.ArrangementDrawer;
import log.charter.gui.handlers.CharterFrameMouseMotionListener;

public class ChartPanel extends JPanel {
	private static final List<Drawer> DRAWERS = asList(//
			new BeatsDrawer(), //
			new ArrangementDrawer(), //
			new SelectedNotesDrawer(), //
			new HighlightDrawer());

	private static final long serialVersionUID = -3439446235287039031L;

	public static final int sectionNamesY = 10;
	public static final int lyricLinesY = sectionNamesY + 20;
	public static final int textY = lyricLinesY + 10;
	public static final int beatTextY = textY + 20;
	public static final int beatSizeTextY = beatTextY + 15;
	public static final int lanesTop = beatSizeTextY + 5;
	public static final int lanesBottom = lanesTop + 250;
	public static final int HEIGHT = lanesBottom + 20;

	public static final int lanesHeight = lanesBottom - lanesTop;

	private static int applyInvertion(final int lane, final int lanesNo) {
		return Config.invertStrings ? lane : lanesNo - lane - 1;
	}

	public static int getLaneY(final int lane, final int lanesNo) {
		return lanesTop + (int) (ChartPanel.lanesHeight * (applyInvertion(lane, lanesNo) + 0.5) / lanesNo);
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

	private final ChartData data;

	public ChartPanel(final ChartEventsHandler handler) {
		super();
		data = handler.data;
		addMouseListener(handler);
		addMouseMotionListener(new CharterFrameMouseMotionListener(data));
	}

	@Override
	public void paintComponent(final Graphics g) {
		data.time = (int) data.nextT;
		g.setColor(ChartPanelColors.get(ColorLabel.BACKGROUND));
		g.fillRect(0, 0, getWidth(), getHeight());
		if (data.isEmpty) {
			return;
		}

		g.setColor(ChartPanelColors.get(ColorLabel.NOTE_BACKGROUND));
		g.fillRect(0, lanesTop, getWidth(), lanesHeight);

		for (final Drawer drawer : DRAWERS) {
			drawer.draw(g, this, data);
		}

		g.setColor(ChartPanelColors.get(ColorLabel.MARKER));
		g.drawLine(Config.markerOffset, lanesTop - 5, Config.markerOffset, lanesBottom);
		if (data.editMode == EditMode.GUITAR && data.isNoteAdd) {
			g.setColor(ChartPanelColors.get(ColorLabel.NOTE_ADD_LINE));
			g.drawLine(data.mousePressX, data.mousePressY, data.mx, data.my);
		}
	}
}
