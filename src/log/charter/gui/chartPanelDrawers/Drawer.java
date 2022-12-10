package log.charter.gui.chartPanelDrawers;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;

public interface Drawer {

	void draw(final Graphics g, final ChartPanel panel, final ChartData data);
}
