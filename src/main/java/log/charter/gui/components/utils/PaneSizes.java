package log.charter.gui.components.utils;

import static log.charter.data.config.GraphicalConfig.inputSize;

public class PaneSizes {
	public int width;
	public int verticalSpace;
	public int rowHeight;
	public int rowSpacing;
	public int rowDistance;

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

	public void rowHeight(final int rowHeight) {
		this.rowHeight = rowHeight;
		rowDistance = rowHeight + rowSpacing;
	}

	public void rowSpacing(final int rowSpacing) {
		this.rowSpacing = rowSpacing;
		rowDistance = rowHeight + rowSpacing;
	}

	public void setDefaultSizes() {
		verticalSpace = inputSize / 2;
		rowHeight = inputSize;
		rowSpacing = inputSize / 4;
		rowDistance = rowHeight + rowSpacing;
	}
}