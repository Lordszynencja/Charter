package log.charter.util;

public class Position2D {
	public final int x;
	public final int y;

	public Position2D(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public Position2D move(final int offsetX, final int offsetY) {
		return new Position2D(x + offsetX, y + offsetY);
	}
}
