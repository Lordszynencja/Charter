package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

import log.charter.util.Position2D;

class StrokedTriangle implements DrawableShape {
	private final Position2D a;
	private final Position2D b;
	private final Position2D c;
	private final Color color;
	private final int thickness;

	public StrokedTriangle(final Position2D a, final Position2D b, final Position2D c, final Color color) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.color = color;
		this.thickness = 1;
	}

	public StrokedTriangle(final Position2D a, final Position2D b, final Position2D c, final Color color, final int thickness) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.color = color;
		this.thickness = thickness;
	}

	@Override
	public void draw(final Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(color);
		g2.setStroke(new BasicStroke(thickness));
		g2.drawPolygon(new int[] { a.x, b.x, c.x }, new int[] { a.y, b.y, c.y }, 3);
	}
}
