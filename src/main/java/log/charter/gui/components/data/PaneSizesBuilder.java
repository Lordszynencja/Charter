package log.charter.gui.components.data;

public class PaneSizesBuilder {
	public int width;
	public int verticalSpace = 10;
	public int rowHeight = 20;
	public int rowSpacing = 5;

	public PaneSizesBuilder(final int width) {
		this.width = width;
	}

	public PaneSizesBuilder verticalSpace(final int verticalSpace) {
		this.verticalSpace = verticalSpace;
		return this;
	}

	public PaneSizesBuilder rowHeight(final int rowHeight) {
		this.rowHeight = rowHeight;
		return this;
	}

	public PaneSizesBuilder rowSpacing(final int rowSpacing) {
		this.rowSpacing = rowSpacing;
		return this;
	}

	public PaneSizes build() {
		return new PaneSizes(width, verticalSpace, rowHeight, rowSpacing);
	}
}