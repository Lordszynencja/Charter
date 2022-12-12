package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;

class CenteredTextWithBackground implements DrawableShape {
	private final ShapePosition position;
	private final String text;
	private final Color backgroundColor;
	private final Color textColor;

	public CenteredTextWithBackground(final ShapePosition position, final String text, final Color backgroundColor,
			final Color textColor) {
		this.position = position;
		this.text = text;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
	}

	@Override
	public void draw(final Graphics g) {
		final int width = g.getFontMetrics().stringWidth(text);
		final int height = g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent();
		final int offsetX = width / 2;
		final int offsetY = height / 2;

		final int textX = position.x - offsetX;
		final int textY = position.y + offsetY;

		if (backgroundColor != null) {
			final int bgY = textY - height;
			g.setColor(backgroundColor);
			g.fillRect(textX, bgY - 1, width, height + 2);
		}

		g.setColor(textColor);
		g.drawString(text, textX, textY - 1);
	}

}
