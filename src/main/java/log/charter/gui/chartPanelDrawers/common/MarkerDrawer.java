package log.charter.gui.chartPanelDrawers.common;

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
	private static final int arrowSize = 8;
	private static final Font font = new Font(Font.DIALOG, Font.PLAIN, 10);

	private void drawLine(final Graphics2D g, final int x) {
		g.setStroke(new BasicStroke(1));
		g.drawLine(x, beatTextY - 5, x, lanesBottom);
	}

	private void drawArrow(final Graphics2D g, final int x) {
		final Polygon arrowhead = new Polygon();
		arrowhead.addPoint(x - arrowSize / 2, beatTextY - 10);
		arrowhead.addPoint(x + arrowSize / 2, beatTextY - 10);
		arrowhead.addPoint(x, beatTextY - 10 + arrowSize);
		g.fillPolygon(arrowhead);
	}

	private void drawTime(final Graphics2D g, final int x, final double time) {
		final int hours = (int) (time / 3_600_000);
		final int minutes = ((int) (time / 60_000)) % 60;
		final int seconds = ((int) time / 1000) % 60;
		final int miliseconds = ((int) time) % 1000;

		final Position2D position = new Position2D(x + arrowSize / 2 + 2, 2);
		final String timeString = "%d:%02d:%02d.%03d".formatted(hours, minutes, seconds, miliseconds);

		new TextWithBackground(position, font, timeString, ColorLabel.MARKER_TIME, ColorLabel.MARKER_TIME_BACKGROUND,
				ColorLabel.MARKER_TIME_BACKGROUND).draw(g);
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
