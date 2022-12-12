package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

class Text implements DrawableShape {
	private final ShapePosition position;
	private final String text;
	private final Color color;

	public Text(final ShapePosition position, final String text, final Color color) {
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
