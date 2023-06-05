package log.charter.gui.components.preview3D;

import java.util.Comparator;

public class Point3D {
	public static Comparator<Point3D> scenePositionComparator = (a, b) -> {
		if (a.y != b.y) {
			return a.y > b.y ? 1 : -1;
		}
		if (a.z != b.z) {
			return a.z > b.z ? -1 : 1;
		}
		if (a.x != b.x) {
			return a.x > b.x ? 1 : -1;
		}

		return 0;
	};

	public final double x, y, z, w;

	public Point3D(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		w = 1;
	}

	public Point3D(final double x, final double y, final double z, final double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Point3D multiply(final Matrix4 matrix) {
		final double newX = x * matrix.matrix[0][0] //
				+ y * matrix.matrix[0][1]//
				+ z * matrix.matrix[0][2]//
				+ w * matrix.matrix[0][3];
		final double newY = x * matrix.matrix[1][0]//
				+ y * matrix.matrix[1][1]//
				+ z * matrix.matrix[1][2]//
				+ w * matrix.matrix[1][3];
		final double newZ = x * matrix.matrix[2][0]//
				+ y * matrix.matrix[2][1]//
				+ z * matrix.matrix[2][2]//
				+ w * matrix.matrix[2][3];
		final double newW = x * matrix.matrix[3][0]//
				+ y * matrix.matrix[3][1]//
				+ z * matrix.matrix[3][2]//
				+ w * matrix.matrix[3][3];

		return new Point3D(newX, newY, newZ, newW);
	}

	public Point3D descale() {
		return new Point3D(x / w, y / w, z / 2, 1);
	}
}
