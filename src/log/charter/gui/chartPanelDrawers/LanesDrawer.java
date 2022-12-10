package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.getLaneY;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class LanesDrawer implements Drawer {
	private static void drawVocalLane(final Graphics g, final int width) {
		g.setColor(ChartPanelColors.get(ColorLabel.LANE));
		final int y = getLaneY(0, 1);
		g.drawLine(0, y, width, y);
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.isEmpty) {
			return;
		}

		if (data.editMode == EditMode.VOCALS) {
			drawVocalLane(g, panel.getWidth());
		}
	}
}
