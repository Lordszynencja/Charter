package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

class FilledOval implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;

	public FilledOval(final ShapePositionWithSize position, final Color color) {
		this.position = position;
		this.color = color;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillOval(position.x, position.y, position.width, position.height);
	}
}
