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
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.ChartPanel;
import log.charter.gui.components.toolbar.ChartToolbar;

public class WaveFormDrawer {
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

	private static final Color normalColor = new Color(64, 128, 128);
	private static final Color highIntensityColor = new Color(255, 128, 255);
	private static final Color normalColorZoomed = new Color(64, 128, 128, 32);
	private static final Color highIntensityColorZoomed = new Color(255, 128, 255, 64);

	private ChartData data;
	private ChartPanel chartPanel;
	private ChartToolbar chartToolbar;
	private ModeManager modeManager;

	private boolean drawWaveForm;

	public void init(final ChartData data, final ChartPanel chartPanel, final ChartToolbar chartToolbar,
			final ModeManager modeManager) {
		this.data = data;
		this.chartPanel = chartPanel;
		this.chartToolbar = chartToolbar;
		this.modeManager = modeManager;
	}

	public void toggle() {
		drawWaveForm = !drawWaveForm;
		chartToolbar.updateValues();
	}

	public boolean drawing() {
		return drawWaveForm || modeManager.getMode() == EditMode.TEMPO_MAP;
	}

	private void drawApproximate(final Graphics g) {
		int position = 0;
		int maxValue = 0;
		int minValue = 0;
		boolean highIntensity = false;

		final float timeToFrameMultiplier = data.music.outFormat.getFrameRate() / 1000;

		final int[] musicValues = data.music.data[0];
		int start = (int) (xToTime(0, data.time) * timeToFrameMultiplier);
		start = max(1, start);
		int end = (int) (xToTime(chartPanel.getWidth(), data.time) * timeToFrameMultiplier);
		end = min(musicValues.length, end);

		final int midY = (lanesBottom + lanesTop) / 2;
		final int yScale = (lanesBottom - lanesTop) * 9 / 20;
		final RMSCalculator rmsCalculator = new RMSCalculator(musicValues, (int) timeToFrameMultiplier, start);

		for (int frame = start; frame < end; frame++) {
			final int newPosition = timeToX(frame / timeToFrameMultiplier, data.time);
			final int newHeight = musicValues[frame] * yScale / 0x8000;
			if (newPosition > position) {
				g.setColor(highIntensity ? highIntensityColor : normalColor);
				final int height = max(-minValue, maxValue);
				g.drawLine(position, midY + height, position, midY - height);

				position = newPosition;
				maxValue = 0;
				minValue = 0;
				highIntensity = false;
			}

			if (newHeight > maxValue) {
				maxValue = newHeight;
			}
			if (newHeight < minValue) {
				minValue = newHeight;
			}

			rmsCalculator.addValue(musicValues[frame]);
			if (!highIntensity && rmsCalculator.getRMS() > 4) {
				highIntensity = true;
			}
		}
	}

	private void drawFull(final Graphics g) {
		final float timeToFrameMultiplier = data.music.outFormat.getFrameRate() / 1000;

		final int[] musicValues = data.music.data[0];
		int start = (int) (xToTime(0, data.time) * timeToFrameMultiplier);
		start = max(1, start);
		int end = (int) (xToTime(chartPanel.getWidth(), data.time) * timeToFrameMultiplier);
		end = min(musicValues.length, end);

		final int midY = (lanesBottom + lanesTop) / 2;
		final int yScale = (lanesBottom - lanesTop) / 2;
		int x0 = 0;
		int x1 = timeToX((int) ((start - 1) / timeToFrameMultiplier), data.time);
		int y0 = 0;
		int y1 = musicValues[start - 1] * yScale / 0x8000;
		final RMSCalculator rmsCalculator = new RMSCalculator(musicValues, (int) timeToFrameMultiplier, start);

		for (int frame = start; frame < end; frame++) {
			x0 = x1;
			x1 = timeToX(frame / timeToFrameMultiplier, data.time);
			y0 = y1;
			y1 = musicValues[frame] * yScale / 0x8000;

			rmsCalculator.addValue(musicValues[frame]);

			g.setColor(rmsCalculator.getRMS() > 4 ? highIntensityColorZoomed : normalColorZoomed);
			g.drawLine(x0, midY + y0, x1, midY + y1);
		}
	}

	public void draw(final Graphics g) {
		if (!drawing()) {
			return;
		}

		if (Zoom.zoom > 2) {
			drawFull(g);
		} else {
			drawApproximate(g);
		}
	}
}
