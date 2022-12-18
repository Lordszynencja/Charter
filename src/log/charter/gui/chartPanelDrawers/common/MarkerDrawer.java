package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;

import java.awt.Graphics;

import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class MarkerDrawer {
	public void draw(final Graphics g) {
		g.setColor(ChartPanelColors.get(ColorLabel.MARKER));
		g.drawLine(Config.markerOffset, lanesTop - 5, Config.markerOffset, lanesBottom);
	}
}
