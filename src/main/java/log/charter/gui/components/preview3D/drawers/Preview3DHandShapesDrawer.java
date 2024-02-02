package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.gui.components.preview3D.data.HandShapeDrawData.getHandShapesForTimeSpanWithRepeats;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.HandShapeDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;

public class Preview3DHandShapesDrawer {
	private static final double lineThickness = 0.012;

	private ChartData data;
	private RepeatManager repeatManager;

	public void init(final ChartData data, final RepeatManager repeatManager) {
		this.data = data;
		this.repeatManager = repeatManager;
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
		final double x0 = x - lineThickness;
		final double x1 = x - lineThickness / 2;
		final double x2 = x + lineThickness / 2;
		final double x3 = x + lineThickness;

		addSquare(drawData, x0, x1, y, z0, z1, alpha, color);
		addSquare(drawData, x1, x2, y, z0, z1, color, color);
		addSquare(drawData, x2, x3, y, z0, z1, color, alpha);
	}

	private void addHandShape(final BaseShaderDrawData drawData, final HandShapeDrawData handShape) {
		final double y = getChartboardYPosition(data.currentStrings()) + 0.0002;
		final boolean arpeggio = handShape.template.arpeggio;

		final int timeFrom = max(0, handShape.timeFrom - data.time);
		final int timeTo = min(visibility, handShape.timeTo - data.time);
		if (timeTo < 0) {
			return;
		}
		final double z0 = getTimePosition(timeFrom);
		final double z1 = getTimePosition(timeTo);

		final Color color = (arpeggio ? ColorLabel.PREVIEW_3D_ARPEGGIO : ColorLabel.PREVIEW_3D_LANE_BORDER).color();
		final Color alpha = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		addThickLine(drawData, handShape.fretFrom, y, z0, z1, color, alpha);
		addThickLine(drawData, handShape.fretTo, y, z0, z1, color, alpha);
	}

	public void draw(final ShadersHolder shadersHolder) {
		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();
		final List<HandShapeDrawData> handShapesToDraw = getHandShapesForTimeSpanWithRepeats(data, repeatManager,
				data.time, data.time + visibility);

		handShapesToDraw.forEach(handShape -> addHandShape(drawData, handShape));

		drawData.draw(GL30.GL_QUADS, Matrix4.identity);
	}
}
