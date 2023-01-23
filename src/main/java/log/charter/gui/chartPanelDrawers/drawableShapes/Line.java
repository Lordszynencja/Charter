package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

import log.charter.util.Position2D;

class Line implements DrawableShape {
	private final Position2D startPosition;
	private final Position2D endPosition;
	private final Color color;
	private final int thickness;

	public Line(final Position2D startPosition, final Position2D endPosition, final Color color) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.color = color;
		this.thickness = 1;
	}

	public Line(final Position2D startPosition, final Position2D endPosition, final Color color, final int thickness) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.color = color;
		this.thickness = thickness;
	}

	@Override
	public void draw(final Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(color);
		g2.setStroke(new BasicStroke(thickness));
		g2.drawLine(startPosition.x, startPosition.y, endPosition.x, endPosition.y);
	}

}
