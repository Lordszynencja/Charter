package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.util.data.Position2D;

public class StrokedTriangle implements DrawableShape {
	private final Position2D a;
	private final Position2D b;
	private final Position2D c;
	private final Color color;
	private final int thickness;

	public StrokedTriangle(final Position2D a, final Position2D b, final Position2D c, final ColorLabel color) {
		this(a, b, c, color.color(), 1);
	}

	public StrokedTriangle(final Position2D a, final Position2D b, final Position2D c, final Color color) {
		this(a, b, c, color, 1);
	}

	public StrokedTriangle(final Position2D a, final Position2D b, final Position2D c, final Color color,
			final int thickness) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.color = color;
		this.thickness = thickness;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.setStroke(new BasicStroke(thickness));
		g.drawPolygon(new int[] { a.x, b.x, c.x }, new int[] { a.y, b.y, c.y }, 3);
	}
}
