package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.util.Position2D;

public class TextWithBackground implements DrawableShape {
	private final Text text;
	private final Color backgroundColor;
	private final int space;
	//private final Color borderColor; // Added gray border color

	public TextWithBackground(final Position2D position, final Font font, final String text, final ColorLabel textColor,
							  final ColorLabel backgroundColor, final Color borderColor) {
		this(position, font, text, textColor.color(), backgroundColor.color(), 1, borderColor);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final ColorLabel textColor,
							  final ColorLabel backgroundColor, final int space, final Color borderColor) {
		this(position, font, text, textColor.color(), backgroundColor.color(), space, borderColor);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final Color textColor,
							  final Color backgroundColor, final Color borderColor) {
		this(position, font, text, textColor, backgroundColor, 1, borderColor);
	}

	public TextWithBackground(final Position2D position, final Font font, final String text, final Color textColor,
							  final Color backgroundColor, final int space, final Color borderColor) {
		this.text = new Text(position.move(space, space), font, text, textColor);
		this.backgroundColor = backgroundColor;
		this.space = space;
		//this.borderColor = borderColor;
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
		// Draw the rounded rectangle background
		Shape roundedRect = new RoundRectangle2D.Double(
				positionAndSize.x - space, positionAndSize.y - space,
				positionAndSize.width + 2 * space, positionAndSize.height + 2 * space,
				5, 5);

		/* Draw the gray border
		g.setColor(borderColor);
		g.draw(roundedRect); */

		// Draw the background
		g.setColor(backgroundColor);
		g.fill(roundedRect);

		// Draw the text
		text.draw(g, positionAndSize.resized(space, space, -2 * space, -2 * space - 1));
	}
}
