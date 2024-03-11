package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import log.charter.util.data.Position2D;

class FilledDiamond implements DrawableShape {
	private final Position2D position;
	private final int radius;
	private final Color color;

	public FilledDiamond(final Position2D position, final int radius, final Color color) {
		this.position = position.move(1, 0);
		this.radius = radius;
		this.color = color;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);

		final Polygon polygon = new Polygon(
				new int[] { position.x - radius, position.x, position.x + radius, position.x }, //
				new int[] { position.y, position.y - radius, position.y, position.y + radius }, //
				4);
		g.fillPolygon(polygon);
	}
}