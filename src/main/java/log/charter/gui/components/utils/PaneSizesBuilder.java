package log.charter.gui.components.utils;

import static log.charter.data.config.GraphicalConfig.inputSize;

public class PaneSizesBuilder {
	public int width;
	public int verticalSpace = inputSize / 2;
	public int rowHeight = inputSize;
	public int rowSpacing = inputSize / 4;

	public PaneSizesBuilder(final int width) {
		this.width = width;
	}

	public PaneSizesBuilder(final PaneSizes base) {
		width = base.width;
		verticalSpace = base.verticalSpace;
		rowHeight = base.rowHeight;
		rowSpacing = base.rowSpacing;
	}

	public PaneSizesBuilder width(final int value) {
		width = value;
		return this;
	}

	public PaneSizesBuilder verticalSpace(final int value) {
		verticalSpace = value;
		return this;
	}

	public PaneSizesBuilder rowHeight(final int value) {
		rowHeight = value;
		return this;
	}

	public PaneSizesBuilder rowSpacing(final int value) {
		rowSpacing = value;
		return this;
	}

	public PaneSizes build() {
		return new PaneSizes(width, verticalSpace, rowHeight, rowSpacing);
	}
}