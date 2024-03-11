package log.charter.gui.chartPanelDrawers.drawableShapes;

import log.charter.util.data.Position2D;

public class ShapePositionWithSize extends Position2D {
	public final int width;
	public final int height;

	public ShapePositionWithSize(final int x, final int y, final int width, final int height) {
		super(x, y);
		this.width = width;
		this.height = height;
	}

	public int getBottomY() {
		return y + height;
	}

	public int getRightX() {
		return x + width;
	}

	public ShapePositionWithSize centered() {
		return new ShapePositionWithSize(x - width / 2, y - height / 2, width, height);
	}

	public ShapePositionWithSize centeredX() {
		return new ShapePositionWithSize(x - width / 2, y, width, height);
	}

	public ShapePositionWithSize centeredY() {
		return new ShapePositionWithSize(x, y - height / 2, width, height);
	}

	public ShapePositionWithSize resized(final int xOffset, final int yOffset, final int widthOffset,
			final int heightOffset) {
		return new ShapePositionWithSize(x + xOffset, y + yOffset, width + widthOffset, height + heightOffset);
	}

	public ShapePositionWithSizeDouble asDouble() {
		return new ShapePositionWithSizeDouble(x, y, width, height);
	}
}
