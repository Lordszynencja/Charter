package log.charter.gui.components.preview3D.glUtils;

public class Point2D {

	public final double x, y, w;

	public Point2D(final double x, final double y) {
		this.x = x;
		this.y = y;
		w = 1;
	}

	public Point2D(final double x, final double y, final double w) {
		this.x = x;
		this.y = y;
		this.w = w;
	}

	public Point2D descale() {
		return new Point2D(x / w, y / w, 1);
	}
}
