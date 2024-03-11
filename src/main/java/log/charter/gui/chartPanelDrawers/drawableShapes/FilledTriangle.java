package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.util.data.Position2D;

class FilledTriangle implements DrawableShape {
	private final Position2D a;
	private final Position2D b;
	private final Position2D c;
	private final Color color;

	public FilledTriangle(final Position2D a, final Position2D b, final Position2D c, final Color color) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.color = color;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillPolygon(new int[] { a.x, b.x, c.x }, new int[] { a.y, b.y, c.y }, 3);
	}
}
