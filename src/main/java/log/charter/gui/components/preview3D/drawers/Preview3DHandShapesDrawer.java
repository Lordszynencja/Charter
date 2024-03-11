package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.fadedDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.fretThickness;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getVisibility;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.HandShapeDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.FadingShaderDrawData;
import log.charter.util.data.IntRange;

public class Preview3DHandShapesDrawer {
	private static final double lineThickness0 = fretThickness * 2;
	private static final double lineThickness1 = fretThickness * 6;

	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void addSquare(final FadingShaderDrawData drawData, final double x0, final double x1, final double y,
			final double z0, final double z1, final Color color0, final Color color1) {
		drawData.addVertex(new Point3D(x0, y, z0), color0)//
				.addVertex(new Point3D(x1, y, z0), color1)//
				.addVertex(new Point3D(x1, y, z1), color1)//
				.addVertex(new Point3D(x0, y, z1), color0);
	}

	private void addThickLine(final FadingShaderDrawData drawData, final int fret, final double y, final double z0,
			final double z1, final Color color, final Color alpha) {
		final double x = getFretPosition(fret);
		final double x0 = x - lineThickness1;
		final double x1 = x - lineThickness0;
		final double x2 = x + lineThickness0;
		final double x3 = x + lineThickness1;

		addSquare(drawData, x0, x1, y, z0, z1, alpha, color);
		addSquare(drawData, x1, x2, y, z0, z1, color, color);
		addSquare(drawData, x2, x3, y, z0, z1, color, alpha);
	}

	private void addHandShape(final Preview3DDrawData drawData, final FadingShaderDrawData shaderDrawData,
			final HandShapeDrawData handShape) {
		final double y = getChartboardYPosition(data.currentStrings()) + 0.0002;
		final boolean arpeggio = handShape.template.arpeggio;

		final int timeFrom = max(0, handShape.timeFrom - drawData.time);
		final int timeTo = min(getVisibility(), handShape.timeTo - drawData.time);
		if (timeTo < 0) {
			return;
		}
		final double z0 = getTimePosition(timeFrom);
		final double z1 = getTimePosition(timeTo);

		final Color color = (arpeggio ? ColorLabel.PREVIEW_3D_ARPEGGIO : ColorLabel.PREVIEW_3D_LANE_BORDER).color();
		final Color alpha = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		final IntRange frets = drawData.getFrets(handShape.originalPosition);

		addThickLine(shaderDrawData, frets.min - 1, y, z0, z1, color, alpha);
		addThickLine(shaderDrawData, frets.max, y, z0, z1, color, alpha);
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final FadingShaderDrawData shaderDrawData = shadersHolder.new FadingShaderDrawData();

		drawData.handShapes.forEach(handShape -> addHandShape(drawData, shaderDrawData, handShape));

		shaderDrawData.draw(GL30.GL_QUADS, Matrix4.identity, closeDistanceZ, fadedDistanceZ);
	}
}
