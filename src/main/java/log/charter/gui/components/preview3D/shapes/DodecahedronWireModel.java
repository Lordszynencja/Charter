package log.charter.gui.components.preview3D.shapes;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.glUtils.Point3D;

public class DodecahedronWireModel implements Model {

	public static DodecahedronWireModel instance = new DodecahedronWireModel();

	private static List<Point3D> points;

	static {
		final double scale = 5;
		final double x = 0.05 * scale;
		final double y = 0.2 * scale;
		final double z = 0.1 * scale;

		final double[] ys = { -y, -y / 3, y / 3, y };
		final Point3D[][] levelPoints = new Point3D[4][5];
		for (int i = 0; i < 4; i++) {
			final double levelRadius = (i == 0 || i == 3) ? 0.5 : 0.7;
			for (int j = 0; j < 5; j++) {
				final double angle = (i % 2 == 0 ? 0 : Math.PI / 5) + Math.PI * 2 / 5 * j;
				levelPoints[i][j] = new Point3D(Math.cos(angle) * x * levelRadius, ys[i],
						Math.sin(angle) * z * levelRadius);
			}
		}

		points = new ArrayList<Point3D>();

		for (int level = 0; level < 4; level++) {
			for (int i = 0; i < 5; i++) {
				points.add(levelPoints[level][i]);
				points.add(levelPoints[level][(i + 1) % 5]);
			}
		}

		for (int i = 0; i < 5; i++) {
			points.add(levelPoints[0][i]);
			points.add(levelPoints[1][i]);
			points.add(levelPoints[0][(i + 1) % 5]);
			points.add(levelPoints[1][i]);

			points.add(levelPoints[2][i]);
			points.add(levelPoints[1][i]);
			points.add(levelPoints[2][(i + 1) % 5]);
			points.add(levelPoints[1][i]);

			points.add(levelPoints[2][i]);
			points.add(levelPoints[3][i]);
			points.add(levelPoints[2][(i + 1) % 5]);
			points.add(levelPoints[3][i]);
		}
	}

	@Override
	public List<Point3D> getPoints() {
		return points;
	}

	@Override
	public int getDrawMode() {
		return GL30.GL_LINES;
	}
}
