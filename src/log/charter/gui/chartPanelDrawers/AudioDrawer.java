package log.charter.gui.chartPanelDrawers;

import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Color;
import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;

public class AudioDrawer implements Drawer {
	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.isEmpty || !data.drawAudio) {
			return;
		}

		final int zero = (int) ((xToTime(0, data.time) * data.music.outFormat.getFrameRate()) / 1000);
		int start = zero;
		int end = (int) ((xToTime(panel.getWidth(), data.time) * data.music.outFormat.getFrameRate()) / 1000);
		final double multiplier = ((double) panel.getWidth()) / (end - start);
		if (start < 0) {
			start = 0;
		}

		final int[] musicValues = data.music.data[0];
		if (end > musicValues.length) {
			end = musicValues.length;
		}

		final int midY = (ChartPanel.lanesBottom + ChartPanel.lanesTop) / 2;

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
		g.setColor(ChartPanelColors.get(ColorLabel.MARKER));
		for (int i = start; i < end; i += step) {
			x0 = x1;
			x1 += xStep;
			y0 = y1;
			y1 = musicValues[i] / 320;

			g.setColor(new Color(64, 128, 128, 32));
			g.drawLine((int) x0, midY + y0, (int) x1, midY + y1);
		}
	}
}
