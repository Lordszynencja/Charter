package log.charter.gui.chartPanelDrawers.drawableShapes;

public class ShapePositionWithSize extends ShapePosition {
	public final int width;
	public final int height;

	public ShapePositionWithSize(final int x, final int y, final int width, final int height) {
		super(x, y);
		this.width = width;
		this.height = height;
	}

	public ShapePositionWithSize centered() {
		return new ShapePositionWithSize(x - width / 2, y - height / 2, width, height);
	}
}
