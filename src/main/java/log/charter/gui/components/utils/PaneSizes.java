package log.charter.gui.components.utils;

public class PaneSizes {
	public final int width;
	public final int verticalSpace;
	public final int rowHeight;
	public final int rowSpacing;
	public final int rowDistance;

	public PaneSizes(final int width, final int verticalSpace, final int rowHeight, final int rowSpacing) {
		this.width = width;
		this.verticalSpace = verticalSpace;
		this.rowHeight = rowHeight;
		this.rowSpacing = rowSpacing;
		rowDistance = rowHeight + rowSpacing;
	}

	public int getY(final int row) {
		return verticalSpace + row * rowDistance;
	}

	public int getHeight(final int rows) {
		return verticalSpace * 2 + rowHeight * rows + rowSpacing * (rows - 1);
	}
}