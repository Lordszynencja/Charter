package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

public class StrokedRectangle implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;

	public StrokedRectangle(final int x, final int y, final int width, final int height, final Color color) {
		position = new ShapePositionWithSize(x, y, width, height);
		this.color = color;
	}

	public StrokedRectangle(final ShapePositionWithSize position, final Color color) {
		this.position = position;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		g.setColor(color);
		g.drawRect(position.x, position.y, position.width, position.height);
	}

	public StrokedRectangle centered() {
		return new StrokedRectangle(position.centered(), color);
	}
}