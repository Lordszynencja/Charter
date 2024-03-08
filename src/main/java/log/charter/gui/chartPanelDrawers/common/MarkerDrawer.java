package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class MarkerDrawer {
	public void draw(final Graphics g) {
		g.setColor(ColorLabel.MARKER.color());
		final Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1));
		g.drawLine(Config.markerOffset, beatTextY - 5, Config.markerOffset, lanesBottom);

		// Draw arrowhead
		final int arrowSize = 8;
		final Polygon arrowhead = new Polygon();
		arrowhead.addPoint(Config.markerOffset - arrowSize / 2, beatTextY - 10);
		arrowhead.addPoint(Config.markerOffset + arrowSize / 2, beatTextY - 10);
		arrowhead.addPoint(Config.markerOffset, beatTextY - 10 + arrowSize);
		g2.fillPolygon(arrowhead);
	}
}
