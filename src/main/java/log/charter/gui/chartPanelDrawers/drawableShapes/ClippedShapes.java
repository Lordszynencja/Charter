package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.List;

class ClippedShapes implements DrawableShape {
	private final ShapePositionWithSize position;
	private final List<DrawableShape> shapes;

	public ClippedShapes(final ShapePositionWithSize position, final List<DrawableShape> shapes) {
		this.position = position;
		this.shapes = shapes;
	}

	@Override
	public void draw(final Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Shape oldClip = g.getClip();
		g.setClip(position.x, position.y, position.width, position.height);
		for (final DrawableShape shape : shapes) {
			shape.draw(g);
		}

		g.setClip(oldClip);
	}

}
