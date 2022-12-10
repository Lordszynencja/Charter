package log.charter.gui.chartPanelDrawers.drawableShapes;

import java.awt.Graphics;

public class FilledRectangle implements DrawableShape {
	public final int x;
	public final int y;
	public final int width;
	public final int height;

	public FilledRectangle(final int x, final int y, final int width, final int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void draw(final Graphics g) {
		g.fillRect(x, y, width, height);
	}
}