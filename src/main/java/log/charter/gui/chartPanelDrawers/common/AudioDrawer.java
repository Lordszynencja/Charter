package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Color;
import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.config.Zoom;
import log.charter.gui.ChartPanel;

public class AudioDrawer {
	private static class RMSCalculator {
		private int counter = 0;
		private final float[] values;

		public RMSCalculator(final int[] musicValues, final int frameRate, final int start) {
			values = new float[frameRate];

			for (int i = 0; i < frameRate; i++) {
				final int position = max(0, min(musicValues.length - 1, start - frameRate + i));
				addValue(musicValues[position]);
			}
		}

		public void addValue(final int val) {
			values[counter++ % values.length] = val * val;
		}

		public double getRMS() {
			float sum = 0;
			for (final float value : values) {
				sum += value;
			}

			return sqrt(sum) / 0x7FFF;
		}
	}

	private static final int midY = (lanesBottom + lanesTop) / 2;
	private static final Color normalColor = new Color(64, 128, 128);
	private static final Color highIntensityColor = new Color(255, 128, 255);
	private static final Color normalColorZoomed = new Color(64, 128, 128, 32);
	private static final Color highIntensityColorZoomed = new Color(255, 128, 255, 64);

	private ChartData data;
	private ChartPanel chartPanel;

	public boolean drawAudio;

	public void init(final ChartData data, final ChartPanel chartPanel) {
		this.data = data;
		this.chartPanel = chartPanel;
	}

	public void draw(final Graphics g) {
		if (!drawAudio) {
			return;
		}

		final float timeToFrameMultiplier = data.music.outFormat.getFrameRate() / 1000;

		final int[] musicValues = data.music.data[0];
		int start = (int) (xToTime(0, data.time) * timeToFrameMultiplier);
		start = max(1, start);
		int end = (int) (xToTime(chartPanel.getWidth(), data.time) * timeToFrameMultiplier);
		end = min(musicValues.length, end);

		int x0;
		int x1 = timeToX((int) ((start - 1) / timeToFrameMultiplier), data.time);
		int y0 = 0;
		int y1 = musicValues[start - 1] / 320;
		final RMSCalculator rmsCalculator = new RMSCalculator(musicValues, (int) timeToFrameMultiplier, start);

		for (int frame = start; frame < end; frame++) {
			x0 = x1;
			x1 = timeToX(frame / timeToFrameMultiplier, data.time);
			y0 = y1;
			y1 = musicValues[frame] / 320;

			rmsCalculator.addValue(musicValues[frame]);

			if (Zoom.zoom < 0.5) {
				if (rmsCalculator.getRMS() > 4) {
					g.setColor(highIntensityColor);
				}

				if (frame % 100 == 0) {
					g.drawLine(x0, midY - y0, x0, midY + y0);
					g.setColor(normalColor);
				}
			} else {
				if (rmsCalculator.getRMS() > 4) {
					g.setColor(highIntensityColorZoomed);
				} else {
					g.setColor(normalColorZoomed);
				}

				g.drawLine(x0, midY + y0, x1, midY + y1);
			}
		}
	}
}
