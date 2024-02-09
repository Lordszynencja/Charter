package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.util.Position2D;

public class Line implements DrawableShape {
	private final Position2D startPosition;
	private final Position2D endPosition;
	private final Color color;
	private final int thickness;

	public Line(final Position2D startPosition, final Position2D endPosition, final Color color) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.color = color;
		thickness = 1;
	}

	public Line(final Position2D startPosition, final Position2D endPosition, final Color color, final int thickness) {
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.color = color;
		this.thickness = thickness;
	}

	@Override
	public void draw(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.setStroke(new BasicStroke(thickness));
		g2.drawLine(startPosition.x, startPosition.y, endPosition.x, endPosition.y);
	}

}
