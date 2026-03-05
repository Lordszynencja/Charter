package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

class StrokedRoundRectangle implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;

	private final int thickness;
	private final int arc;

	public StrokedRoundRectangle(final int x, final int y, final int width, final int height, final Color color) {
		position = new ShapePositionWithSize(x, y, width, height);
		this.color = color;
		thickness = 1;
		arc = 5;
	}

	public StrokedRoundRectangle(final ShapePositionWithSize position, final Color color) {
		this.position = position;
		this.color = color;
		thickness = 1;
		arc = 5;
	}

	public StrokedRoundRectangle(final ShapePositionWithSize position, final Color color, final int thickness,
			final int arc) {
		this.position = position;
		this.color = color;
		this.thickness = thickness;
		this.arc = arc;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);

		final Shape roundedRect = new RoundRectangle2D.Double(position.x, position.y, position.width, position.height,
				arc, arc);
		g.setStroke(new BasicStroke(thickness));
		g.draw(roundedRect);
	}

	public StrokedRoundRectangle centered() {
		return new StrokedRoundRectangle(position.centered(), color, thickness, arc);
	}
}