package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

class FilledArc implements DrawableShape {
	private final ShapePositionWithSize position;
	private final int startAngle;
	private final int arcAngle;
	private final Color color;

	public FilledArc(final ShapePositionWithSize position, final int startAngle, final int arcAngle,
			final Color color) {
		this.position = position;
		this.startAngle = startAngle;
		this.arcAngle = arcAngle;
		this.color = color;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillArc(position.x, position.y, position.width, position.height, startAngle, arcAngle);
	}
}
