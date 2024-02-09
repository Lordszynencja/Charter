package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

class StrokedRectangle implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;

	private final int thickness;

	public StrokedRectangle(final int x, final int y, final int width, final int height, final Color color) {
		position = new ShapePositionWithSize(x, y, width, height);
		this.color = color;
		thickness = 1;
	}

	public StrokedRectangle(final ShapePositionWithSize position, final Color color) {
		this.position = position;
		this.color = color;
		thickness = 1;
	}

	public StrokedRectangle(final ShapePositionWithSize position, final Color color, final int thickness) {
		this.position = position;
		this.color = color;
		this.thickness = thickness;
	}

	@Override
	public void draw(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.setStroke(new BasicStroke(thickness));
		g2.drawRect(position.x, position.y, position.width, position.height);
	}

	public StrokedRectangle centered() {
		return new StrokedRectangle(position.centered(), color);
	}
}