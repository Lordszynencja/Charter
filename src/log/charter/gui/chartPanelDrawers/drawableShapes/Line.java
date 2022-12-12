package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

class Line implements DrawableShape {
	private final ShapePosition startPosition;
	private final ShapePosition endPosition;
	private final Color color;

	public Line(final ShapePosition startPosition, final ShapePosition endPosition, final Color color) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		g.setColor(color);
		g.drawLine(startPosition.x, startPosition.y, endPosition.x, endPosition.y);
	}

}
