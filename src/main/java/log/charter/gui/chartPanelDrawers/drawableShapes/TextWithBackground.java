package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.util.Position2D;

public class TextWithBackground implements DrawableShape {
	private final Text text;
	private final Color backgroundColor;
	private final int space;

	public TextWithBackground(final Position2D position, final Font font, final String text, final ColorLabel textColor,
			final ColorLabel backgroundColor) {
		this(position, font, text, textColor.color(), backgroundColor.color(), 1);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final ColorLabel textColor,
			final ColorLabel backgroundColor, final int space) {
		this(position, font, text, textColor.color(), backgroundColor.color(), space);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final Color textColor,
			final Color backgroundColor) {
		this(position, font, text, textColor, backgroundColor, 1);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final Color textColor,
			final Color backgroundColor, final int space) {
		this.text = new Text(position.move(space, space), font, text, textColor);
		this.backgroundColor = backgroundColor;
		this.space = space;
	}

	@Override
	public void draw(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		draw(g2, getPositionWithSize(g));
	}

	public ShapePositionWithSize getPositionWithSize(final Graphics g) {
		return text.getPositionWithSize(g).resized(-space, -space, 2 * space, 2 * space + 1);
	}

	public void draw(final Graphics2D g, final ShapePositionWithSize positionAndSize) {
		g.setColor(backgroundColor);
		g.fillRect(positionAndSize.x, positionAndSize.y, positionAndSize.width, positionAndSize.height);

		text.draw(g, positionAndSize.resized(space, space, -2 * space, -2 * space - 1));
	}

}
