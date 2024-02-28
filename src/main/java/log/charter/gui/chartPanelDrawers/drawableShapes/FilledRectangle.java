package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import log.charter.gui.ChartPanelColors.ColorLabel;

public class FilledRectangle implements DrawableShape {
	private final ShapePositionWithSize position;
	private final Color color;

	public FilledRectangle(final int x, final int y, final int width, final int height, final ColorLabel color) {
		this(new ShapePositionWithSize(x, y, width, height), color.color());
	}

	public FilledRectangle(final int x, final int y, final int width, final int height, final Color color) {
		this(new ShapePositionWithSize(x, y, width, height), color);
	}

	public FilledRectangle(final ShapePositionWithSize position, final Color color) {
		this.position = position;
		this.color = color;
	}

	@Override
	public void draw(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(color);
		g2.fillRect(position.x, position.y, position.width, position.height);
	}

	public FilledRectangle centered() {
		return new FilledRectangle(position.centered(), color);
	}
}