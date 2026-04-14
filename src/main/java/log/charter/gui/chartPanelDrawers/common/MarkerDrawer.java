package log.charter.gui.chartPanelDrawers.common;

import static log.charter.data.config.GraphicalConfig.chartTextHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.config.GraphicalConfig;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
import log.charter.util.data.Position2D;

public class MarkerDrawer {
	private static int arrowSize = 8;
	private static Font font = new Font(Font.DIALOG, Font.PLAIN, 10);

	public static void reloadGraphics() {
		arrowSize = beatTextY / 2;
		font = new Font(Font.DIALOG, Font.PLAIN, chartTextHeight);
	}

	private void drawLine(final Graphics2D g, final int x) {
		g.setStroke(new BasicStroke(1));
		g.drawLine(x, beatTextY - 5, x, lanesBottom);
	}

	private void drawArrow(final Graphics2D g, final int x) {
		final int x0 = x - arrowSize / 2;
		final int x1 = x;
		final int x2 = x + 1;
		final int x3 = x2 + arrowSize / 2;

		final Polygon arrowhead = new Polygon();
		arrowhead.addPoint(x0, beatTextY / 3);
		arrowhead.addPoint(x3, beatTextY / 3);
		arrowhead.addPoint(x1, beatTextY);
		arrowhead.addPoint(x2, beatTextY);
		g.fillPolygon(arrowhead);
	}

	private void drawTime(final Graphics2D g, final int x, final double time) {
		final int hours = (int) (time / 3_600_000);
		final int minutes = ((int) (time / 60_000)) % 60;
		final int seconds = ((int) time / 1000) % 60;
		final int miliseconds = ((int) time) % 1000;

		final Position2D position = new Position2D(x + arrowSize / 2 + 2, 2);
		final String timeString = "%d:%02d:%02d.%03d".formatted(hours, minutes, seconds, miliseconds);

		new TextWithBackground(position, font, timeString, ColorLabel.MARKER_TIME, ColorLabel.MARKER_TIME_BACKGROUND)
				.draw(g);
	}

	public void draw(final Graphics g, final double time) {
		final int x = GraphicalConfig.markerOffset;

		g.setColor(ColorLabel.MARKER.color());

		final Graphics2D g2 = (Graphics2D) g;
		drawLine(g2, x);
		drawArrow(g2, x);
		drawTime(g2, x, time);
	}
}
