package log.charter.gui.components.preview3D;

import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibilityZ;

import java.awt.Color;

import org.lwjgl.opengl.GL33;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.BaseShader.BaseShaderDrawData;

public class Preview3DFretLanesDrawer {
	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final BaseShader baseShader) {
		final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();
		final Color color = ColorLabel.PREVIEW_3D_FRET_LANE.color();
		final double y = getChartboardYPosition(data.currentStrings());

		for (int i = 0; i <= Config.frets; i++) {
			final double x = getFretPosition(i);
			drawData.addVertex(new Point3D(x, y, 0), color)//
					.addVertex(new Point3D(x, y, visibilityZ), color);
		}

		drawData.draw(GL33.GL_LINES);
	}
}
