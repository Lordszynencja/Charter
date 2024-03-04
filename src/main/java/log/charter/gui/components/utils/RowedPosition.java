package log.charter.gui.components.utils;

import log.charter.gui.components.data.PaneSizes;

public class RowedPosition {
	private final int startX;
	private final int startY;
	private final int rowHeight;

	private int x;
	private int y;

	private RowedPosition(final int startX, final int startY, final int rowHeight, final int x, final int y) {
		this.startX = startX;
		this.startY = startY;
		this.rowHeight = rowHeight;
		this.x = x;
		this.y = y;
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

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getAndAddX(final int offset) {
		final int value = x;
		x += offset;
		return value;
	}

	public void newRows(final int rows) {
		x = startX;
		y += rowHeight * rows;
	}

	public void newRow() {
		newRows(1);
	}

	public void newRowsInPlace(final int rows) {
		y += rowHeight * rows;
	}

	public void newRowInPlace() {
		newRowsInPlace(1);
	}

	public void setPosition(final int offset, final int row) {
		x = startX + offset;
		y = startY + rowHeight * row;
	}

	public RowedPosition copy() {
		return new RowedPosition(startX, startY, rowHeight, x, y);
	}
}
