package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

class FilledOval implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;

	public FilledOval(final ShapePositionWithSize position, final Color color) {
		this.position = position;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.fillOval(position.x, position.y, position.width, position.height);
	}
}
