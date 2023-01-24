package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;

import log.charter.util.Position2D;

class CenteredTextWithBackground implements DrawableShape {
	private final Position2D position;
	private final String text;
	private final Color backgroundColor;
	private final Color textColor;

	public CenteredTextWithBackground(final Position2D position, final String text, final Color backgroundColor,
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
		final int height = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
		final int offsetX = width / 2;
		final int offsetY = height / 2;

		final int textX = position.x - offsetX;
		final int textY = position.y + offsetY;

		if (backgroundColor != null) {
			final int bgY = textY - height;
			g2.setColor(Color.BLACK);
			g2.fillRect(textX - 1, bgY - 3, width + 3, height + 5);

			g2.setColor(backgroundColor);
			g2.fillRect(textX, bgY - 2, width + 1, height + 3);
		}

		g2.setColor(textColor);
		g2.drawString(text, textX + 1, textY - 1);
	}

}
