package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

public class Text implements DrawableShape {

	private final String text;
	private final int x;
	private final int y;
	private final Color color;

	public Text(final String text, final int x, final int y, final Color color) {
		this.text = text;
		this.x = x;
		this.y = y;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		g.setColor(color);
		g.drawString(text, x, y);
	}
}
