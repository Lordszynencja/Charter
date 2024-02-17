package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;

import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class MarkerDrawer {
	public void init() {
	}

	public void draw(final Graphics g) {
		g.setColor(ColorLabel.MARKER.color());
		final Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1));
		g.drawLine(Config.markerOffset, lanesTop - 5, Config.markerOffset, lanesBottom);
	}
}
