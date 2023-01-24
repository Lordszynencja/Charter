package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

import log.charter.util.Position2D;

class FilledPolygon implements DrawableShape {
	private final Position2D[] points;
	private final Color color;

	public FilledPolygon(final Position2D[] points, final Color color) {
		this.points = points;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		final int[] xs = new int[points.length];
		final int[] ys = new int[points.length];
		for (int i = 0; i < points.length; i++) {
			xs[i] = points[i].x;
			ys[i] = points[i].y;
		}

		g2.fillPolygon(xs, ys, points.length);
	}
}
