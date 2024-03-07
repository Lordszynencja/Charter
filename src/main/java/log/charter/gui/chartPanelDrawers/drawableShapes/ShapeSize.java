package log.charter.gui.chartPanelDrawers.drawableShapes;

public class ShapeSize {
	public final int width;
	public final int height;

	public ShapeSize(final int width, final int height) {
		this.width = width;
		this.height = height;
	}

	public ShapeSize resizeBy(final int widthOffset, final int heightOffset) {
		return new ShapeSize(width + widthOffset, height + heightOffset);
	}
}
