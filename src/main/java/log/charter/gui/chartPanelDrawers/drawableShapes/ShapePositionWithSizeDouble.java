package log.charter.gui.chartPanelDrawers.drawableShapes;

public class ShapePositionWithSizeDouble {
	public final double x;
	public final double y;
	public final double width;
	public final double height;

	public ShapePositionWithSizeDouble(final double x, final double y, final double width, final double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public ShapePositionWithSizeDouble centered() {
		return new ShapePositionWithSizeDouble(x - width / 2, y - height / 2, width, height);
	}

	public ShapePositionWithSizeDouble centeredX() {
		return new ShapePositionWithSizeDouble(x - width / 2, y, width, height);
	}

	public ShapePositionWithSizeDouble centeredY() {
		return new ShapePositionWithSizeDouble(x, y - height / 2, width, height);
	}

	public ShapePositionWithSizeDouble resized(final int xOffset, final int yOffset, final int widthOffset,
			final int heightOffset) {
		return new ShapePositionWithSizeDouble(x + xOffset, y + yOffset, width + widthOffset, height + heightOffset);
	}

	public ShapePositionWithSize asInteger() {
		return new ShapePositionWithSize((int) x, (int) y, (int) width, (int) height);
	}
}
