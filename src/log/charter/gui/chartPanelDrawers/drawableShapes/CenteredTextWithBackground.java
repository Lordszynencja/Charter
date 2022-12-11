package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

public class CenteredTextWithBackground implements DrawableShape {
	private final int x;
	private final int y;
	private final String text;
	private final Color backgroundColor;
	private final Color textColor;

	public CenteredTextWithBackground(final int x, final int y, final String text) {
		this.x = x;
		this.y = y;
		this.text = text;
		backgroundColor = null;
		textColor = Color.BLACK;
	}

	public CenteredTextWithBackground(final int x, final int y, final String text, final Color textColor) {
		this.x = x;
		this.y = y;
		this.text = text;
		backgroundColor = null;
		this.textColor = textColor;
	}

	public CenteredTextWithBackground(final int x, final int y, final String text, final Color backgroundColor,
			final Color textColor) {
		this.x = x;
		this.y = y;
		this.text = text;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
	}

	@Override
	public void draw(final Graphics g) {
		final int width = g.getFontMetrics().stringWidth(text);
		final int height = g.getFontMetrics().getAscent();
		final int offsetX = width / 2;
		final int offsetY = height / 2;

		if (backgroundColor != null) {
			g.setColor(backgroundColor);
			g.fillRect(x - offsetX - 2, y - offsetY - 1, width + 4, height + 2);
		}
		g.setColor(textColor);
		g.drawString(text, x - offsetX, y + offsetY - 2);
	}

}
