package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.fadedDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.fretThickness;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getVisibilityZ;
import static log.charter.util.ColorUtils.setAlpha;

import java.awt.Color;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.AnchorDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.util.data.IntRange;

public class Preview3DLaneBordersDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	private void drawFretBorder(final ShadersHolder shadersHolder, final double x, final double y, final Color color) {
		final double x0 = x - fretThickness;
		final double x1 = x + fretThickness;

		shadersHolder.new FadingShaderDrawData()//
				.addVertex(new Point3D(x, y, getVisibilityZ()), color)//
				.addVertex(new Point3D(x, y, 0), color)//
				.draw(GL33.GL_LINES, Matrix4.identity, closeDistanceZ, 0);
		shadersHolder.new FadingShaderDrawData()//
				.addVertex(new Point3D(x0, y, getVisibilityZ()), color)//
				.addVertex(new Point3D(x1, y, getVisibilityZ()), color)//
				.addVertex(new Point3D(x0, y, 0), color)//
				.addVertex(new Point3D(x1, y, 0), color)//
				.draw(GL33.GL_TRIANGLE_STRIP, Matrix4.identity, closeDistanceZ, fadedDistanceZ);
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final int[] fretsOpacity = new int[Config.frets + 1];
		for (int fret = 0; fret <= Config.frets; fret++) {
			fretsOpacity[fret] = 32;
		}

		for (final AnchorDrawData anchor : drawData.anchors) {
			for (int fret = anchor.fretFrom; fret <= anchor.fretTo; fret++) {
				fretsOpacity[fret] = max(fretsOpacity[fret], 96);
			}
		}

		final IntRange activeFrets = drawData.getFrets(drawData.time);
		if (activeFrets != null) {
			for (int fret = activeFrets.min - 1; fret <= activeFrets.max; fret++) {
				fretsOpacity[fret] = 255;
			}
		}

		final Color color = ColorLabel.PREVIEW_3D_LANE_BORDER.color();
		final double y = getChartboardYPosition(data.currentStrings());

		GL30.glLineWidth(1);
		for (int fret = 0; fret <= Config.frets; fret++) {
			drawFretBorder(shadersHolder, getFretPosition(fret), y, setAlpha(color, fretsOpacity[fret]));
		}
	}
}
