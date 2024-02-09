package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.util.Position2D;

public class Text implements DrawableShape {
	public static ShapeSize getExpectedSize(final Graphics g, final Font font, final String text) {
		final Graphics2D g2 = (Graphics2D) g;
		g.setFont(font);

		final int width = g2.getFontMetrics().stringWidth(text) + (font.isItalic() ? 1 : 0);
		final int height = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()
				- (font.isItalic() ? 1 : 0);

		return new ShapeSize(width, height);
	}

	private final Position2D position;
	private final Font font;
	private final String text;
	private final Color color;

	public Text(final Position2D position, final Font font, final String text, final ColorLabel color) {
		this(position, font, text, color.color());
	}

	public Text(final Position2D position, final Font font, final String text, final Color color) {
		this.position = position;
		this.font = font;
		this.text = text;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		draw(g2, getPositionWithSize(g));
	}

	public ShapePositionWithSize getPositionWithSize(final Graphics g) {
		final ShapeSize size = getExpectedSize(g, font, text);
		return new ShapePositionWithSize(position.x, position.y, size.width, size.height);
	}

	public void draw(final Graphics2D g, final ShapePositionWithSize positionAndSize) {
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, positionAndSize.x, positionAndSize.y + positionAndSize.height);
	}
}
