package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.util.data.Position2D;

class StrokedPolygon implements DrawableShape {
	private final Position2D[] points;
	private final Color color;

	public StrokedPolygon(final Position2D[] points, final Color color) {
		this.points = points;
		this.color = color;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		final int[] xs = new int[points.length];
		final int[] ys = new int[points.length];
		for (int i = 0; i < points.length; i++) {
			xs[i] = points[i].x;
			ys[i] = points[i].y;
		}

		g.drawPolygon(xs, ys, points.length);
	}
}
