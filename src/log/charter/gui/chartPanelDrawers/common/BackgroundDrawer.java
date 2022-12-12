package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
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
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;

public class BackgroundDrawer {
	private static final int timeScaleTop = DrawerUtils.lanesBottom;
	private static final int nonsecondsMarkerHeight = 10;
	private static final int timestampY = timeScaleTop + 30;

	private static final DecimalFormat twoDigitsFormat = new DecimalFormat("00");

	private ChartData data;
	private ChartPanel chartPanel;

	public void init(final ChartData data, final ChartPanel chartPanel) {
		this.data = data;
		this.chartPanel = chartPanel;
	}

	private void drawBackground(final Graphics g) {
		g.setColor(ChartPanelColors.get(ColorLabel.BACKGROUND));
		g.fillRect(0, 0, chartPanel.getWidth(), chartPanel.getHeight());
	}

	private void drawLanesBackground(final Graphics g) {
		g.setColor(ChartPanelColors.get(ColorLabel.NOTE_BACKGROUND));
		g.fillRect(0, lanesTop, chartPanel.getWidth(), lanesHeight);
	}

	private void drawTimeScale(final Graphics g) {
		int time = xToTime(-20, data.time);
		if (time < 0) {
			time = 0;
		}

		time /= 100;

		final int endTime = xToTime(chartPanel.getWidth() + 20, data.time) / 100;
		if (endTime - time > 1_000) {
			return;
		}

		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
		while (time <= endTime) {
			final int x = timeToX(time * 100, data.time);
			if (time % 10 == 0) {
				filledRectangle(new ShapePositionWithSize(x, timeScaleTop, 2, 20), Color.WHITE).draw(g);
				final String formattedtime = twoDigitsFormat.format(time / 600) + ":"
						+ twoDigitsFormat.format((time / 10) % 60);
				g.drawString(formattedtime, x - 21, timestampY);
			} else {
				lineVertical(x, timeScaleTop, timeScaleTop + nonsecondsMarkerHeight, Color.WHITE).draw(g);
			}

			time++;
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
