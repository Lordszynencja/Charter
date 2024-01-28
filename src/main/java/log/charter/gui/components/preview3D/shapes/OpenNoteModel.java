package log.charter.gui.components.preview3D.shapes;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.util.CollectionUtils.Pair;

public class OpenNoteModel implements CompositeModel {
	private static final int details = 6;

	private final List<Pair<Integer, List<Point3D>>> points;

	public OpenNoteModel(final double width) {
		final List<Point3D> cylinderPoints = new ArrayList<>();
		final List<Point3D> side0Points = new ArrayList<>();
		final List<Point3D> side1Points = new ArrayList<>();

		final double thickness0 = 0.04;
		final double thickness1 = 0.05;

		final double x0 = 0;
		final double x1 = width / 2;
		final double x2 = width;
		double y0 = thickness0 * 1;
		double y1 = thickness1 * 1;
		double z0 = 0;
		double z1 = 0;
		Point3D p0 = new Point3D(x0, y0, z0);
		Point3D p1 = new Point3D(x1, y1, z1);
		Point3D p2 = new Point3D(x2, y0, z0);

		for (int i = 0; i < details; i++) {
			final double angle = Math.PI * (i + 1) * 2 / details;
			final double c = cos(angle);
			y0 = thickness0 * c * 1;
			y1 = thickness1 * c * 1;
			z0 = thickness0 * sin(angle) / 10;
			z1 = thickness1 * sin(angle) / 10;
			final Point3D newP0 = new Point3D(x0, y0, z0);
			final Point3D newP1 = new Point3D(x1, y1, z1);
			final Point3D newP2 = new Point3D(x2, y0, z0);
			cylinderPoints.add(p0);
			cylinderPoints.add(p1);
			cylinderPoints.add(newP1);
			cylinderPoints.add(newP0);
			cylinderPoints.add(p1);
			cylinderPoints.add(p2);
			cylinderPoints.add(newP2);
			cylinderPoints.add(newP1);
			side0Points.add(p0);
			side1Points.add(newP2);
			p0 = newP0;
			p1 = newP1;
			p2 = newP2;
		}

		points = asList(//
				new Pair<>(GL30.GL_QUADS, cylinderPoints), //
				new Pair<>(GL30.GL_POLYGON, side0Points), //
				new Pair<>(GL30.GL_POLYGON, side1Points));
	}

	@Override
	public List<Pair<Integer, List<Point3D>>> getPointsForModes() {
		return points;
	}
}
