package log.charter.gui.components.utils;

public class PaneSizesBuilder {
	public int width;
	public int verticalSpace = 10;
	public int rowHeight = 20;
	public int rowSpacing = 5;

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