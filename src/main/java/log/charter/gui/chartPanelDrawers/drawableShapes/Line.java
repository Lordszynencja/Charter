package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

import log.charter.util.Position2D;

class Line implements DrawableShape {
	private final Position2D startPosition;
	private final Position2D endPosition;
	private final Color color;

	public Line(final Position2D startPosition, final Position2D endPosition, final Color color) {
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
