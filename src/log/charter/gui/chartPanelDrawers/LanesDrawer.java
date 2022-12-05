package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.getLaneY;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;

public class LanesDrawer implements Drawer {
	private static void drawLanes(final Graphics g, final int lanes, final int width) {
		if (lanes <= 0 || lanes > 100) {
			return;
		}
		for (int i = 0; i < lanes; i++) {
			final int y = getLaneY(i, lanes);
			g.drawLine(0, y, width, y);
		}
	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		g.setColor(ChartPanel.colors.get("LANE"));
		int lanesCount = 0;
		if (data.currentInstrument.type.isGuitarType()) {
			lanesCount = 6;
		} else if (data.currentInstrument.type.isDrumsType()) {
			lanesCount = 5;
		} else if (data.currentInstrument.type.isKeysType()) {
			lanesCount = 5;
		} else if (data.currentInstrument.type.isVocalsType()) {
			lanesCount = 1;
		}
		drawLanes(g, lanesCount, panel.getWidth());
	}
}
