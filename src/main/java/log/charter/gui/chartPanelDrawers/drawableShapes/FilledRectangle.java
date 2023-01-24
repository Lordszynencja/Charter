package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

class FilledRectangle implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;

	public FilledRectangle(final int x, final int y, final int width, final int height, final Color color) {
		position = new ShapePositionWithSize(x, y, width, height);
		this.color = color;
	}

	public FilledRectangle(final ShapePositionWithSize position, final Color color) {
		this.position = position;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.fillRect(position.x, position.y, position.width, position.height);
	}

	public FilledRectangle centered() {
		return new FilledRectangle(position.centered(), color);
	}
}