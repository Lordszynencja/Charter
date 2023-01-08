package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

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
		g.setColor(color);
		g.drawString(text, position.x, position.y);
	}
}
