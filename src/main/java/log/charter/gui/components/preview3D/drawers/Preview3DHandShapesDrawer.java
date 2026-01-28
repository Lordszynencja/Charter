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
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.HandShapeDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseTextureShaderDrawData;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.FadingShaderDrawData;
import log.charter.util.data.IntRange;

public class Preview3DHandShapesDrawer {
	private static final double lineThickness0 = fretThickness * 2;
	private static final double lineThickness1 = fretThickness * 6;
	private static final double chordNameSize = 0.7;

	private ChartData data;
	private TextTexturesHolder textTexturesHolder;

	public void init(final ChartData data, final TextTexturesHolder textTexturesHolder) {
		this.data = data;
		this.textTexturesHolder = textTexturesHolder;
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

	private void addChordName(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final BaseTextureShaderDrawData texturesShader, final HandShapeDrawData handShape, final int fretFrom,
			final double z) {
		if (handShape.timeFrom - drawData.time <= 0 && handShape.timeTo - drawData.time < 150) {
			return;
		}

		final BufferedTextureData textureData = textTexturesHolder.setTextInTexture(handShape.template.chordName, 64f,
				ColorLabel.PREVIEW_3D_CHORD_NAME.color(), true);

		final double x0 = fretFrom - 1.75;
		final double x1 = x0 + chordNameSize * textureData.width / textureData.height;
		final double y1 = getChartboardYPosition(0) + 0.5;
		final double y0 = y1 + chordNameSize;

		shadersHolder.new BaseTextureShaderDrawData()//
				.addZQuad(x0, x1, y0, y1, z, 0, 1, 0, 1)//
				.draw(GL30.GL_QUADS, Matrix4.identity, textTexturesHolder.getTextureId());
	}

	private void addHandShape(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final FadingShaderDrawData shaderDrawData, final BaseTextureShaderDrawData texturesShader,
			final HandShapeDrawData handShape) {
		final double y = getChartboardYPosition(data.currentStrings()) + 0.0002;
		final boolean arpeggio = handShape.template.arpeggio;

		final double timeFrom = max(0, handShape.timeFrom - drawData.time);
		final double timeTo = min(getVisibility(), handShape.timeTo - drawData.time);
		if (timeTo < 0) {
			return;
		}

		final IntRange frets = drawData.getFrets(handShape.originalPosition);
		if (frets == null) {
			return;
		}

		final double z0 = getTimePosition(timeFrom);
		final double z1 = getTimePosition(timeTo);

		final Color color = (arpeggio ? ColorLabel.PREVIEW_3D_ARPEGGIO : ColorLabel.PREVIEW_3D_LANE_BORDER).color();
		final Color alpha = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);

		addThickLine(shaderDrawData, frets.min - 1, y, z0, z1, color, alpha);
		addThickLine(shaderDrawData, frets.max, y, z0, z1, color, alpha);
		addChordName(shadersHolder, drawData, texturesShader, handShape, frets.min, z0);
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final FadingShaderDrawData fadingShader = shadersHolder.new FadingShaderDrawData();
		final BaseTextureShaderDrawData texturesShader = shadersHolder.new BaseTextureShaderDrawData();

		drawData.handShapes
				.forEach(handShape -> addHandShape(shadersHolder, drawData, fadingShader, texturesShader, handShape));

		fadingShader.draw(GL30.GL_QUADS, Matrix4.identity, closeDistanceZ, fadedDistanceZ);
	}
}
