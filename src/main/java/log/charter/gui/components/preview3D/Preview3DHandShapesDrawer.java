package log.charter.gui.components.preview3D;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.song.notes.IPosition.findLastBeforeEqual;
import static log.charter.song.notes.IPosition.findLastIdBeforeEqual;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.BaseShader.BaseShaderDrawData;
import log.charter.song.Anchor;
import log.charter.song.HandShape;
import log.charter.util.CollectionUtils.ArrayList2;

public class Preview3DHandShapesDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void addSquare(final BaseShaderDrawData drawData, final double x0, final double x1, final double y,
			final double z0, final double z1, final Color color0, final Color color1) {
		drawData.addVertex(new Point3D(x0, y, z0), color0)//
				.addVertex(new Point3D(x1, y, z0), color1)//
				.addVertex(new Point3D(x1, y, z1), color1)//
				.addVertex(new Point3D(x0, y, z1), color0);
	}

	private void addThickLine(final BaseShaderDrawData drawData, final int fret, final double y, final double z0,
			final double z1, final Color color, final Color alpha) {
		final double x = getFretPosition(fret);
		final double x0 = x - 0.02;
		final double x1 = x - 0.01;
		final double x2 = x + 0.01;
		final double x3 = x + 0.02;

		addSquare(drawData, x0, x1, y, z0, z1, alpha, color);
		addSquare(drawData, x1, x2, y, z0, z1, color, color);
		addSquare(drawData, x2, x3, y, z0, z1, color, alpha);
	}

	private void addHandShape(final BaseShaderDrawData drawData, final HandShape handShape) {
		final double y = getChartboardYPosition(data.currentStrings()) + 0.0002;

		Anchor anchor = findLastBeforeEqual(data.getCurrentArrangementLevel().anchors, handShape.position());
		if (anchor == null) {
			anchor = new Anchor(0, 0, 0);
		}

		final int timeFrom = max(0, handShape.position() - data.time);
		final int timeTo = min(visibility, handShape.endPosition() - data.time);
		if (timeTo < 0) {
			return;
		}
		final double z0 = getTimePosition(timeFrom);
		final double z1 = getTimePosition(timeTo);

		final Color color = ColorLabel.PREVIEW_3D_FRET_LANE.color();
		final Color alpha = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		addThickLine(drawData, anchor.fret - 1, y, z0, z1, color, alpha);
		addThickLine(drawData, anchor.topFret(), y, z0, z1, color, alpha);
	}

	public void draw(final BaseShader baseShader) {
		final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();

		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		int idFrom = findLastIdBeforeEqual(handShapes, data.time);
		if (idFrom == -1) {
			idFrom = 0;
		}
		final int idTo = findLastIdBeforeEqual(handShapes, data.time + visibility);

		for (int i = idFrom; i <= idTo; i++) {
			addHandShape(drawData, handShapes.get(i));
		}

		drawData.draw(GL30.GL_QUADS);
	}
}
