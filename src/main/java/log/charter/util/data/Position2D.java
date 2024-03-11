package log.charter.util.data;

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

	@Override
	public String toString() {
		return "Position2D{x=" + x + ", y=" + y + "}";
	}
}
