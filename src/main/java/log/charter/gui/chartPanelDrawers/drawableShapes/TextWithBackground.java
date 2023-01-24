package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

import log.charter.util.Position2D;

class TextWithBackground implements DrawableShape {
	private final Position2D position;
	private final String text;
	private final Color backgroundColor;
	private final Color textColor;

	public TextWithBackground(final Position2D position, final String text, final Color backgroundColor,
			final Color textColor) {
		this.position = position;
		this.text = text;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
	}

	@Override
	public void draw(final Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final int width = g2.getFontMetrics().stringWidth(text);
		final int height = g2.getFontMetrics().getAscent() - g.getFontMetrics().getDescent();

		g2.setColor(backgroundColor);
		g2.fillRect(position.x, position.y - height - 2, width + 2, height + 5);

		g2.setColor(textColor);
		g2.drawString(text, position.x + 1, position.y - 1);
	}

}
