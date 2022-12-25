package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;

import java.awt.Graphics;

import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class MarkerDrawer {
	public void draw(final Graphics g) {
		g.setColor(ColorLabel.MARKER.color());
		g.drawLine(Config.markerOffset, lanesTop - 5, Config.markerOffset, lanesBottom);
	}
}
