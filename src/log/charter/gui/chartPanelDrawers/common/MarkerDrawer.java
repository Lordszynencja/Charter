package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.util.ScalingUtils.timeToX;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class MarkerDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final Graphics g) {
		g.setColor(ColorLabel.MARKER.color());
		g.drawLine(Config.markerOffset, lanesTop - 5, Config.markerOffset, lanesBottom);

		final int startX = timeToX(0, data.time);
		g.drawLine(startX, lanesTop + 30, startX, lanesBottom - 30);
		final int endX = timeToX(data.songChart.beatsMap.songLengthMs, data.time);
		g.drawLine(endX, lanesTop + 30, endX, lanesBottom - 30);
	}
}
