package log.charter.gui.chartPanelDrawers;

import static log.charter.gui.ChartPanel.getLaneY;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;

public class AudioDrawer implements Drawer {
	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.drawAudio) {
			final int zero = (int) ((data.xToTime(0) * data.music.outFormat.getFrameRate()) / 1000);
			int start = zero;
			int end = (int) ((data.xToTime(panel.getWidth()) * data.music.outFormat.getFrameRate()) / 1000);
			final double multiplier = ((double) panel.getWidth()) / (end - start);
			if (start < 0) {
				start = 0;
			}

			final int[] musicValues = data.music.data[0];
			if (end > musicValues.length) {
				end = musicValues.length;
			}

			final int midY = getLaneY(0, 1);

			int step = 1;
			double xStep = multiplier;
			while (xStep < 0.1) {
				step++;
				xStep += multiplier;
			}
			start -= start % step;

			double x0 = 0;
			double x1 = -xStep + ((start - zero) * multiplier);
			int y0 = 0;
			int y1 = 0;
			for (int i = start; i < end; i += step) {
				x0 = x1;
				x1 += xStep;
				y0 = y1;
				y1 = musicValues[i] / 320;
				g.drawLine((int) x0, midY + y0, (int) x1, midY + y1);
			}
		}
	}
}
