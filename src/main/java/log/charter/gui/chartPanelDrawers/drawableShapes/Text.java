package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

import log.charter.util.Position2D;

class Text implements DrawableShape {
	private final Position2D position;
	private final String text;
	private final Color color;

	public Text(final Position2D position, final String text, final Color color) {
		this.position = position;
		this.text = text;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.drawString(text, position.x, position.y);
	}
}
