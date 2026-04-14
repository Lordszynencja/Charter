package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

class FilledRoundRectangle implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;
	private final int arc;

	public FilledRoundRectangle(final int x, final int y, final int width, final int height, final Color color) {
		this(new ShapePositionWithSize(x, y, width, height), color, 5);
	}

	public FilledRoundRectangle(final ShapePositionWithSize position, final Color color) {
		this(position, color, 5);
	}

	public FilledRoundRectangle(final ShapePositionWithSize position, final Color color, final int arc) {
		this.position = position;
		this.color = color;
		this.arc = arc;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);

		final Shape roundedRect = new RoundRectangle2D.Double(position.x, position.y, position.width, position.height,
				arc, arc);
		g.fill(roundedRect);
	}

	public FilledRoundRectangle centered() {
		return new FilledRoundRectangle(position.centered(), color, arc);
	}
}