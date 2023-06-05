package log.charter.gui.components.preview3D;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.BaseShader.BaseShaderDrawData;

public class Preview3DStringsDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final BaseShader baseShader) {
		final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();
		final double x0 = Preview3DUtils.getFretPosition(0);
		final double x1 = Preview3DUtils.getFretPosition(Config.frets);

		for (int i = 0; i < data.currentStrings(); i++) {
			final Color stringColor = ColorLabel.valueOf("LANE_" + i).color();
			final double y = Preview3DUtils.getStringPosition(i);
			drawData.addVertex(new Point3D(x0, y, 0), stringColor)//
					.addVertex(new Point3D(x1, y, 0), stringColor);
		}

		drawData.draw(GL30.GL_LINES);
	}
}
