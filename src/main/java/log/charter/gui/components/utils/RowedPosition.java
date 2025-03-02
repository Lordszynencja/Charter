package log.charter.gui.components.utils;

public class RowedPosition {
	private final int startX;
	private final int startY;
	private final int rowHeight;

	private int x;
	private int y;
	private int row = 0;

	private RowedPosition(final int startX, final int startY, final int rowHeight, final int x, final int y,
			final int row) {
		this.startX = startX;
		this.startY = startY;
		this.rowHeight = rowHeight;
		this.x = x;
		this.y = y;
		this.row = row;
	}

	public RowedPosition(final int baseX, final PaneSizes sizes) {
		startX = baseX;
		startY = sizes.getY(0);
		rowHeight = sizes.rowDistance;
		x = startX;
		y = startY;
	}

	public RowedPosition(final int x, final int y, final int rowHeight) {
		startX = x;
		startY = y;
		this.rowHeight = rowHeight;
		this.x = x;
		this.y = y;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int row() {
		return row;
	}

	public int getAndAddX(final int offset) {
		final int value = x;
		x += offset;
		return value;
	}

	public RowedPosition addX(final int offset) {
		x += offset;
		return this;
	}

	public RowedPosition newRows(final int rows) {
		x = startX;
		y += rowHeight * rows;
		row += rows;

		return this;
	}

	public RowedPosition newRow() {
		newRows(1);
		return this;
	}

	public RowedPosition newRowsInPlace(final int rows) {
		y += rowHeight * rows;
		row += rows;

		return this;
	}

	public RowedPosition newRowInPlace() {
		newRowsInPlace(1);
		return this;
	}

	public RowedPosition setPosition(final int offset, final int row) {
		x = startX + offset;
		y = startY + rowHeight * row;
		this.row = row;

		return this;
	}

	public RowedPosition copy() {
		return new RowedPosition(startX, startY, rowHeight, x, y, row);
	}

	public RowedPosition startFromHere() {
		return new RowedPosition(x, y, rowHeight);
	}
}
