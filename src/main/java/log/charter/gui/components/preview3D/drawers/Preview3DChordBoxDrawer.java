package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.moveMatrix;
import static log.charter.gui.components.preview3D.glUtils.Matrix4.scaleMatrix;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.song.enums.Mute;
import log.charter.gui.components.preview3D.data.ChordBoxDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shapes.ChordBoxHolderModel;
import log.charter.util.ColorUtils;
import log.charter.util.data.IntRange;

public class Preview3DChordBoxDrawer {
	private static final double frameThickness = 0.075;

	private ChartData chartData;

	public void init(final ChartData chartData) {
		this.chartData = chartData;
	}

	private static void drawChordBoxHolder(final ShadersHolder shadersHolder, final double x0, final double x1,
			final double y, final double z) {
		final Color backgroundColor = ColorLabel.PREVIEW_3D_CHORD_BOX.color();
		final Color color = ColorLabel.PREVIEW_3D_CHORD_BOX_DARK.color();

		final Matrix4 modelMatrixL = moveMatrix(x0, y, z - 0.01);
		final Matrix4 modelMatrixR = moveMatrix(x1, y, z - 0.01)//
				.multiply(scaleMatrix(-1, 1, 1));
		shadersHolder.new BaseShaderDrawData()//
				.addPoints(ChordBoxHolderModel.backgroundPoints, backgroundColor)//
				.addPoints(ChordBoxHolderModel.points, color)//
				.draw(ChordBoxHolderModel.drawMode, modelMatrixL)//
				.draw(ChordBoxHolderModel.drawMode, modelMatrixR);
	}

	private static void drawChordBoxFrameHorizontalBar(final ShadersHolder shadersHolder, final double x0,
			final double x1, final double y, final double z, final Color color, final Color darkColor) {
		final double middleX = (x1 + x0) / 2;
		final double y1 = y + frameThickness;

		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(x0, y, z), color)//
				.addVertex(new Point3D(x0, y1, z), color)//
				.addVertex(new Point3D(middleX, y, z), darkColor)//
				.addVertex(new Point3D(middleX, y1, z), darkColor)//
				.addVertex(new Point3D(x1, y, z), color)//
				.addVertex(new Point3D(x1, y1, z), color)//
				.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
	}

	private static void drawChordBoxFrameSidesToTheTop(final ShadersHolder shadersHolder, final double x0,
			final double x1, final double y0, final double y1, final double z, final Color color) {
		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(0, y0, z), color)//
				.addVertex(new Point3D(frameThickness, y0, z), color)//
				.addVertex(new Point3D(0, y1, z), color)//
				.addVertex(new Point3D(frameThickness, y1, z), color)//
				.draw(GL30.GL_TRIANGLE_STRIP, moveMatrix(x0, 0, 0))//
				.draw(GL30.GL_TRIANGLE_STRIP, moveMatrix(x1, 0, 0).multiply(scaleMatrix(-1, 1, 1)));
	}

	private static void drawChordBoxFrameSidesShort(final ShadersHolder shadersHolder, final double x0, final double x1,
			final double y0, final double y1, final double z, final Color color) {
		final Color faded = ColorUtils.transparent(color);
		final double fadeStartY = (y1 + y0) / 2;

		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(0, y0, z), color)//
				.addVertex(new Point3D(frameThickness, y0, z), color)//
				.addVertex(new Point3D(0, fadeStartY, z), color)//
				.addVertex(new Point3D(frameThickness, fadeStartY, z), color)//
				.addVertex(new Point3D(0, y1, z), faded)//
				.addVertex(new Point3D(frameThickness, y1, z), faded)//
				.draw(GL30.GL_TRIANGLE_STRIP, moveMatrix(x0, 0, 0))//
				.draw(GL30.GL_TRIANGLE_STRIP, moveMatrix(x1, 0, 0).multiply(scaleMatrix(-1, 1, 1)));
	}

	private static void drawChordBoxFrame(final ShadersHolder shadersHolder, final double x0, final double x1,
			final double y0, final double y1, final double z, final boolean full) {
		final Color color = ColorLabel.PREVIEW_3D_CHORD_BOX.colorWithAlpha(128);
		final Color darkColor = ColorLabel.PREVIEW_3D_CHORD_BOX_DARK.colorWithAlpha(128);

		drawChordBoxFrameHorizontalBar(shadersHolder, x0, x1, y0, z, color, darkColor);

		if (full) {
			drawChordBoxFrameSidesToTheTop(shadersHolder, x0, x1, y0, y1, z, color);
			drawChordBoxFrameHorizontalBar(shadersHolder, x0, x1, y1, z, color, darkColor);
		} else {
			drawChordBoxFrameSidesShort(shadersHolder, x0, x1, y0, y1, z, color);
		}
	}

	private void drawChordBoxFilling(final ShadersHolder shadersHolder, final double x0, final double x1,
			final double y0, final double y1, final double z) {
		final double middleX = (x1 + x0) / 2;
		final Color color = ColorLabel.PREVIEW_3D_CHORD_BOX.colorWithAlpha(32);
		final Color darkColor = ColorLabel.PREVIEW_3D_CHORD_BOX_DARK.colorWithAlpha(32);

		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(x0, y0, z), color)//
				.addVertex(new Point3D(x0, y1, z), color)//
				.addVertex(new Point3D(middleX, y0, z), darkColor)//
				.addVertex(new Point3D(middleX, y1, z), darkColor)//
				.addVertex(new Point3D(x1, y0, z), color)//
				.addVertex(new Point3D(x1, y1, z), color)//
				.draw(GL30.GL_TRIANGLE_STRIP, Matrix4.identity);
	}

	private static void drawFullChordMute(final ShadersHolder shadersHolder, final double x0, final double x1,
			final double y0, final double y1, double z) {
		final double x = (x0 + x1) / 2;
		final double y = (y0 + y1) / 2;
		final double d0y = 0.8 * (y1 - y);
		final double d1y = 0.95 * (y1 - y);
		final double d0x = d0y;
		final double d1x = d1y;
		z -= 0.001;

		final Color color = new Color(128, 216, 255);
		ColorLabel.PREVIEW_3D_CHORD_FULL_MUTE.color();
		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(x - d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y + d1y, z), color)//
				.addVertex(new Point3D(x + d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y - d1y, z), color)//

				.addVertex(new Point3D(x + d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y - d1y, z), color)//
				.addVertex(new Point3D(x - d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y + d1y, z), color)//
				.draw(GL30.GL_QUADS, Matrix4.identity);
	}

	private static void drawPalmChordMute(final ShadersHolder shadersHolder, final double x0, final double x1,
			final double y0, final double y1, double z) {
		final double x = (x0 + x1) / 2;
		final double y = (y0 + y1) / 2;
		final double d0x = 0.8 * (x1 - x);
		final double d1x = 0.9 * (x1 - x);
		final double d0y = 0.8 * (y1 - y);
		final double d1y = 0.9 * (y1 - y);
		z -= 0.001;

		final Color color = ColorLabel.PREVIEW_3D_CHORD_FULL_MUTE.color();
		shadersHolder.new BaseShaderDrawData()//
				.addVertex(new Point3D(x - d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y + d1y, z), color)//
				.addVertex(new Point3D(x + d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y - d1y, z), color)//

				.addVertex(new Point3D(x + d1x, y + d0y, z), color)//
				.addVertex(new Point3D(x - d0x, y - d1y, z), color)//
				.addVertex(new Point3D(x - d1x, y - d0y, z), color)//
				.addVertex(new Point3D(x + d0x, y + d1y, z), color)//
				.draw(GL30.GL_QUADS, Matrix4.identity);
	}

	private static void drawChordBoxMuteIfNeeded(final ShadersHolder shadersHolder, final ChordBoxDrawData chordBox,
			final double x0, final double x1, final double y0, final double y1, final double z) {
		if (chordBox.mute == Mute.FULL) {
			drawFullChordMute(shadersHolder, x0, x1, y0, y1, z);
		} else if (chordBox.mute == Mute.PALM) {
			drawPalmChordMute(shadersHolder, x0, x1, y0, y1, z);
		}
	}

	public void drawChordBox(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final ChordBoxDrawData chordBox) {
		final IntRange frets = drawData.getFrets(chordBox.position);
		if (frets == null) {
			return;
		}

		final double x0 = getFretPosition(frets.min - 1);
		final double x1 = getFretPosition(frets.max);
		final double y0 = getChartboardYPosition(chartData.currentStrings());
		double y1 = getChartboardYPosition(-1);
		final double z = max(0, getTimePosition(chordBox.position - drawData.time));

		if (chordBox.onlyBox) {
			y1 = (y0 + y1) / 2;
		}

		drawChordBoxHolder(shadersHolder, x0, x1, y0, z);
		drawChordBoxFrame(shadersHolder, x0, x1, y0, y1, z, chordBox.withTop);
		drawChordBoxFilling(shadersHolder, x0, x1, y0, y1, z);
		drawChordBoxMuteIfNeeded(shadersHolder, chordBox, x0, x1, y0, y1, z);
	}

}
