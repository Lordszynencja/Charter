package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;

import java.awt.*;

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

		// Draw arrowhead
		int arrowSize = 8;
		Polygon arrowhead = new Polygon();
		arrowhead.addPoint(Config.markerOffset - arrowSize / 2, lanesTop - 10);
		arrowhead.addPoint(Config.markerOffset + arrowSize / 2, lanesTop - 10);
		arrowhead.addPoint(Config.markerOffset, lanesTop - 10 + arrowSize);
		g2.fillPolygon(arrowhead);
	}
}
