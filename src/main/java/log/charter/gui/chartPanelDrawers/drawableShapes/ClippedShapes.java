package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Graphics;
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
	public void draw(final Graphics g) {
		final Shape oldClip = g.getClip();
		g.setClip(position.x, position.y, position.width, position.height);
		for (final DrawableShape shape : shapes) {
			shape.draw(g);
		}

		g.setClip(oldClip);
	}

}
