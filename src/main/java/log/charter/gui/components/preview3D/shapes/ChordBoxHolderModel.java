package log.charter.gui.components.preview3D.shapes;

import static java.util.Arrays.asList;

import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.gui.components.preview3D.glUtils.Point3D;

public class ChordBoxHolderModel {
	public static final List<Point3D> backgroundPoints = asList(//
			new Point3D(-0.01, -0.01, 0), //
			new Point3D(1.01, -0.01, 0), //
			new Point3D(1.01, 0.11, 0), //
			new Point3D(0.11, 0.11, 0), //
			new Point3D(0.11, 1.11, 0), //
			new Point3D(-0.01, 1.01, 0));
	public static final List<Point3D> points = asList(//
			new Point3D(0, 0, -0.01), //
			new Point3D(1, 0, -0.01), //
			new Point3D(1, 0.1, -0.01), //
			new Point3D(0.1, 0.1, -0.01), //
			new Point3D(0.1, 1.1, -0.01), //
			new Point3D(0, 1, -0.01));

	public static final int drawMode = GL30.GL_TRIANGLE_FAN;
}
