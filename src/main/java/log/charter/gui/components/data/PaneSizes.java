package log.charter.gui.components.data;

public class PaneSizes {
	public int width = 700;
	public int verticalSpace = 10;
	public int rowHeight = 20;
	public int rowSpacing = 5;

	public PaneSizes() {
	}

	public PaneSizes(final int width) {
		this.width = width;
	}

	public PaneSizes rowHeight(final int rowHeight) {
		this.rowHeight = rowHeight;
		return this;
	}

	public PaneSizes rowSpacing(final int rowSpacing) {
		this.rowSpacing = rowSpacing;
		return this;
	}

	public int getY(final int row) {
		return verticalSpace + row * (rowHeight + rowSpacing);
	}

	public int getHeight(final int rows) {
		return verticalSpace * 2 + rowHeight * rows + rowSpacing * (rows - 1);
	}
}