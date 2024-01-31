package log.charter.gui.components.preview3D.drawers;

import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibilityZ;
import static log.charter.util.ColorUtils.transparent;

import java.awt.Color;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;

public class Preview3DLaneBordersDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final ShadersHolder shadersHolder) {
		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();
		final Color color = ColorLabel.PREVIEW_3D_LANE_BORDER.color();
		final Color transparentColor = transparent(color);
		final double y = getChartboardYPosition(data.currentStrings());

		for (int i = 0; i <= Config.frets; i++) {
			final double x = getFretPosition(i);
			drawData.addVertex(new Point3D(x, y, visibilityZ), color)//
					.addVertex(new Point3D(x, y, closeDistanceZ), color)//
					.addVertex(new Point3D(x, y, closeDistanceZ), color)//
					.addVertex(new Point3D(x, y, 0), transparentColor);
		}

		GL30.glLineWidth(1);
		drawData.draw(GL33.GL_LINES, Matrix4.identity);
	}
}
