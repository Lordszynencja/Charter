package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

import log.charter.util.Position2D;

class StrokedTriangle implements DrawableShape {
	private final Position2D a;
	private final Position2D b;
	private final Position2D c;
	private final Color color;

	public StrokedTriangle(final Position2D a, final Position2D b, final Position2D c, final Color color) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		g.setColor(color);
		g.drawPolygon(new int[] { a.x, b.x, c.x }, new int[] { a.y, b.y, c.y }, 3);
	}
}
