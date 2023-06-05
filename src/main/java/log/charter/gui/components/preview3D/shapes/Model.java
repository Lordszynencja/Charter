package log.charter.gui.components.preview3D.shapes;

import java.util.List;

import log.charter.gui.components.preview3D.Point3D;

public interface Model {
	List<Point3D> getPoints();

	public int getDrawMode();
}
