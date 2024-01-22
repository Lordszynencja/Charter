package log.charter.gui.components.preview3D.shapes;

import static java.util.Arrays.asList;

import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.Point3D;

public class FrettedNoteModel implements Model {
	public static final double width = 0.07;
	public static final double height = 0.3;
	public static final double depth = 0.16;

	public static FrettedNoteModel instance = new FrettedNoteModel();

	private static List<Point3D> points;
	static {
		final double x = width / 2;
		final double y = height / 2;
		final double z = depth / 2;
		final Point3D p000 = new Point3D(-x, -y, -z);
		final Point3D p001 = new Point3D(x, -y, -z);
		final Point3D p010 = new Point3D(-x, y, -z);
		final Point3D p011 = new Point3D(x, y, -z);
		final Point3D p100 = new Point3D(-x, -y, z);
		final Point3D p101 = new Point3D(x, -y, z);
		final Point3D p110 = new Point3D(-x, y, z);
		final Point3D p111 = new Point3D(x, y, z);

		points = asList(//
				p000, p001, p011, p010, //
				p000, p100, p101, p100, //
				p000, p010, p110, p100, //
				p100, p101, p111, p110, //
				p010, p110, p111, p110, //
				p001, p011, p111, p101);
	}

	@Override
	public List<Point3D> getPoints() {
		return points;
	}

	@Override
	public int getDrawMode() {
		return GL30.GL_QUADS;
	}

}
