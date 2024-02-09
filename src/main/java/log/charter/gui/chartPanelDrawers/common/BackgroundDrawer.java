package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.editAreaHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.timingY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.util.Position2D;

public class BackgroundDrawer {
	private static final int[] jumpDistances = { 100, 250, 1000, 2000, 5000, 10_000, 30_000, 60_000, 120_000, 300_000,
			600_000, 3_600_000 };
	private static final DecimalFormat twoDigitsFormat = new DecimalFormat("00");

	private static int nonsecondsMarkerBottom = timingY + 12;
	private static int secondsMarkerBottom = timingY + 24;
	private static int textY = timingY + 30;
	private static Font timeFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	public static void reloadSizes() {
		final int y1 = timingY;
		final int y2 = editAreaHeight;

		final int h = y2 - y1;
		nonsecondsMarkerBottom = timingY + (int) (h * 0.2);
		secondsMarkerBottom = timingY + (int) (h * 0.5);
		textY = timingY + (int) (h * 0.65);
		timeFont = new Font(Font.SANS_SERIF, Font.BOLD, (int) (h * 0.4));
	}

	static {
		reloadSizes();
	}

	private ChartData data;
	private ChartPanel chartPanel;

	public void init(final ChartData data, final ChartPanel chartPanel) {
		this.data = data;
		this.chartPanel = chartPanel;
	}

	private void drawBackground(final Graphics g) {
		g.setColor(ColorLabel.BASE_BG_2.color());
		g.fillRect(0, 0, chartPanel.getWidth(), chartPanel.getHeight());
	}

	private void drawLanesBackground(final Graphics g) {
		g.setColor(ColorLabel.BASE_BG_1.color());
		g.fillRect(0, lanesTop, chartPanel.getWidth(), lanesHeight + 1);
	}

	private int calculateJump(final int startTime, final int endTime) {
		final int dt = endTime - startTime;
		final int maxJumpsA = chartPanel.getWidth() / 10;
		final int maxJumpsB = chartPanel.getWidth() / 40;
		for (int i = 0; i < jumpDistances.length; i++) {
			if (dt < jumpDistances[i] * (jumpDistances[i] <= 250 ? maxJumpsA : maxJumpsB)) {
				return jumpDistances[i];
			}
		}

		return jumpDistances[jumpDistances.length - 1];
	}

	private String formatTime(final int t) {
		String formatted = twoDigitsFormat.format(t / 60) + ":" + twoDigitsFormat.format(t % 60);
		if (t > 3600) {
			formatted = t / 3600 + ":" + formatted;
		}
		return formatted;
	}

	private void drawTimestamp(final Graphics g, final int time) {
		final int x = timeToX(time, data.time);
		if (time % 1000 == 0) {
			filledRectangle(new ShapePositionWithSize(x, lanesBottom + 1, 2, secondsMarkerBottom - lanesBottom - 1),
					Color.WHITE).draw(g);

			final String formattedTime = formatTime(time / 1000);
			new CenteredText(new Position2D(x, textY), timeFont, formattedTime, Color.WHITE).draw(g);
		} else {
			lineVertical(x, lanesBottom + 1, nonsecondsMarkerBottom, Color.WHITE).draw(g);
		}
	}

	private void drawTimeScale(final Graphics g) {
		int time = xToTime(-20, data.time);
		if (time < 0) {
			time = 0;
		}

		final int endTime = xToTime(chartPanel.getWidth() + 20, data.time);
		final int jump = calculateJump(time, endTime);
		time -= time % jump;

		while (time <= endTime && time < data.songChart.beatsMap.songLengthMs) {
			drawTimestamp(g, time);
			time += jump;
		}
	}

	public void draw(final Graphics g) {
		drawBackground(g);

		if (data.isEmpty) {
			return;
		}

		drawLanesBackground(g);
		drawTimeScale(g);
	}
}
