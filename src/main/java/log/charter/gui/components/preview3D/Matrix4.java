package log.charter.gui.components.preview3D;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Matrix4 {
	public static final Matrix4 identity = create(1, 0, 0, 0, //
			0, 1, 0, 0, //
			0, 0, 1, 0, //
			0, 0, 0, 1);

	public static Matrix4 create(final double... values) {
		if (values.length != 16) {
			throw new IllegalArgumentException("wrong matrix values size " + values.length);
		}

		return new Matrix4(new double[][] { { values[0], values[1], values[2], values[3] }, //
				{ values[4], values[5], values[6], values[7] }, //
				{ values[8], values[9], values[10], values[11] }, //
				{ values[12], values[13], values[14], values[15] } });
	}

	public static Matrix4 moveMatrix(final double x, final double y, final double z) {
		return create(//
				1, 0, 0, x, //
				0, 1, 0, y, //
				0, 0, 1, z, //
				0, 0, 0, 1);
	}

	public static Matrix4 scaleMatrix(final double x, final double y, final double z) {
		return create(//
				x, 0, 0, 0, //
				0, y, 0, 0, //
				0, 0, z, 0, //
				0, 0, 0, 1);
	}

	public static Matrix4 rotationXMatrix(final double rotation) {
		return create(//
				1, 0, 0, 0, //
				0, cos(rotation), sin(rotation), 0, //
				0, -sin(rotation), cos(rotation), 0, //
				0, 0, 0, 1);
	}

	public static Matrix4 rotationYMatrix(final double rotation) {
		return create(//
				cos(rotation), 0, sin(rotation), 0, //
				0, 1, 0, 0, //
				-sin(rotation), 0, cos(rotation), 0, //
				0, 0, 0, 1);
	}

	public static Matrix4 rotationZMatrix(final double rotation) {
		return create(//
				cos(rotation), sin(rotation), 0, 0, //
				-sin(rotation), cos(rotation), 0, 0, //
				0, 0, 1, 0, //
				0, 0, 0, 1);
	}

	public static Matrix4 cameraMatrix(final double near, final double nearRight, final double nearTop,
			final double far) {
		return create(//
				near / nearRight, 0, 0, 0, //
				0, near / nearTop, 0, 0, //
				0, 0, -(near + far) / (near - far), -2 * far * near / (far - near), //
				0, 0, -1, 0);
	}

	public final double[][] matrix;

	private Matrix4(final double[][] matrix) {
		this.matrix = matrix;
	}

	public Matrix4 multiply(final Matrix4 other) {
		final double[][] newMatrix = new double[4][4];

		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				newMatrix[x][y] = 0;
				for (int i = 0; i < 4; i++) {
					newMatrix[x][y] += matrix[x][i] * other.matrix[i][y];
				}
			}
		}

		return new Matrix4(newMatrix);
	}

}
