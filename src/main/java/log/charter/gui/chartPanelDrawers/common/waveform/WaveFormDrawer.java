package log.charter.gui.chartPanelDrawers.common.waveform;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.waveform.WaveformMap.getSpanForLevel;
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

import log.charter.data.config.Zoom;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.sound.data.AudioData;
import log.charter.util.collections.Pair;

public class WaveFormDrawer {
	private static final Color normalColor = ChartPanelColors.ColorLabel.WAVEFORM_COLOR.color();
	private static final Color highIntensityColor = ChartPanelColors.ColorLabel.WAVEFORM_RMS_COLOR.color();
	private static final Color normalColorZoomed = ChartPanelColors.ColorLabel.WAVEFORM_COLOR.colorWithAlpha(32);
	private static final Color highIntensityColorZoomed = ChartPanelColors.ColorLabel.WAVEFORM_RMS_COLOR
			.colorWithAlpha(64);

	private static int getMiddle() {
		return (lanesBottom + lanesTop) / 2;
	}

	private static int getHeight() {
		return (int) ((lanesBottom - lanesTop) / 2.05);
	}

	private ChartPanel chartPanel;
	private ChartToolbar chartToolbar;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;

	private WaveformMap map = null;
	private boolean drawWaveForm;
	private boolean showRMS;

	public void toggle() {
		drawWaveForm = !drawWaveForm;
		chartToolbar.updateValues();
	}

	public void recalculateMap() {
		map = null;
		new Thread(() -> map = new WaveformMap(projectAudioHandler.getAudio())).start();
	}

	public void toggleRMS() { //
		showRMS = !showRMS;
		chartToolbar.updateValues();
	}

	public boolean rms() { //
		return showRMS;
	}

	public boolean drawing() {
		return drawWaveForm || modeManager.getMode() == EditMode.TEMPO_MAP;
	}

	private void drawFromMap(final Graphics g, final double time) {
		final WaveformMap map = this.map;
		if (map == null) {
			return;
		}

		final Pair<Integer, List<WaveformInformation>> level = map.getLevel(1 / Zoom.zoom);
		final int timeSpan = getSpanForLevel(level.a);

		final int width = chartPanel.getWidth();
		final int y = getMiddle();
		final int fullHeight = getHeight();
		final int start = max(0, (int) (xToPosition(0, time) / timeSpan));
		final int end = min(level.b.size() - 1, (int) (xToPosition(chartPanel.getWidth(), time) / timeSpan));

		final int[] heights = new int[width];
		final boolean[] rms = new boolean[width];

		for (int i = start; i <= end; i++) {
			final WaveformInformation information = level.b.get(i);
			final int x = positionToX(i * timeSpan, time);
			if (x < 0 || x >= width) {
				continue;
			}

			final int height = (int) (information.height * fullHeight);
			heights[x] = Math.max(heights[x], height);
			rms[x] |= showRMS && information.rms;
		}

		final int xStart = max(0, positionToX(0, time));
		final int xEnd = min(width - 1, positionToX(projectAudioHandler.getAudio().msLength(), time));
		for (int x = xStart; x <= xEnd; x++) {
			g.setColor(rms[x] ? highIntensityColor : normalColor);
			final int height = heights[x];

			if (height > 0) {
				g.fillRect(x, y - height, 1, 2 * height + 1);
			} else {
				g.fillRect(x, y, 1, 1);
			}
		}
	}

	private void drawFull(final Graphics g, final double time) {
		final AudioData audio = projectAudioHandler.getAudio();
		final float timeToFrameMultiplier = audio.format.getFrameRate() / 1000;

		final int[] musicValues = audio.data[0];
		int start = (int) (xToPosition(0, time) * timeToFrameMultiplier);
		start = max(1, start);
		int end = (int) (xToPosition(chartPanel.getWidth(), time) * timeToFrameMultiplier);
		end = min(musicValues.length, end);

		final int midY = getMiddle();
		final int yScale = getHeight();
		int x0 = 0;
		int x1 = positionToX((int) ((start - 1) / timeToFrameMultiplier), time);
		int y0 = 0;
		int y1 = (int) ((double) musicValues[start - 1] * yScale / audio.maxValue);
		final RMSCalculator rmsCalculator = new RMSCalculator((int) timeToFrameMultiplier);

		for (int frame = start; frame < end; frame++) {
			x0 = x1;
			x1 = positionToX(frame / timeToFrameMultiplier, time);
			y0 = y1;
			y1 = (int) ((double) musicValues[frame] * yScale / audio.maxValue);

			rmsCalculator.addValue((float) musicValues[frame] / audio.maxValue);

			g.setColor(rmsCalculator.getRMS() > 4 ? highIntensityColorZoomed : normalColorZoomed);
			g.drawLine(x0, midY + y0, x1, midY + y1);
		}
	}

	public void draw(final FrameData frameData) {
		if (!drawing()) {
			return;
		}

		if (Zoom.zoom > 1) {
			drawFull(frameData.g, frameData.time);
		} else {
			drawFromMap(frameData.g, frameData.time);
		}
	}
}
