package log.charter.gui.components.preview3D;

import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;

public class Preview3DStringsDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final ShadersHolder shadersHolder) {
		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();
		final double x0 = getFretPosition(0);
		final double x1 = getFretPosition(Config.frets);

		for (int i = 0; i < data.currentStrings(); i++) {
			final Color stringColor = getStringBasedColor(StringColorLabelType.LANE, i, data.currentStrings());
			final double y = getStringPosition(i, data.currentStrings());
			drawData.addVertex(new Point3D(x0, y, 0), stringColor)//
					.addVertex(new Point3D(x1, y, 0), stringColor);
		}

		for (int i = 0; i <= Config.frets; i++) {
			final Color fretColor = Color.gray;
			final double x = getFretPosition(i);
			final double y0 = Preview3DUtils.topStringPosition + Preview3DUtils.stringDistance / 2;
			final double y1 = Preview3DUtils.topStringPosition
					- Preview3DUtils.stringDistance * (data.currentStrings() - 0.5);
			drawData.addVertex(new Point3D(x, y0, -0.01), fretColor)//
					.addVertex(new Point3D(x, y1, -0.01), fretColor);
		}

		GL30.glLineWidth(2);
		drawData.draw(GL30.GL_LINES, Matrix4.identity);

	}
}
