package log.charter.gui.components.preview3D;

import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;

import java.awt.Color;

import org.lwjgl.opengl.GL33;

import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.BaseShader.BaseShaderDrawData;

public class Preview3DFretLanesDrawer {
	public void draw(final BaseShader baseShader) {
		final BaseShaderDrawData drawData = baseShader.new BaseShaderDrawData();
		final Color color = ColorLabel.PREVIEW_3D_FRET_LANE.color();
		final double y = getStringPosition(6);

		for (int i = 0; i <= Config.frets; i++) {
			final double x = getFretPosition(i);
			drawData.addVertex(new Point3D(x, y, 0), color)//
					.addVertex(new Point3D(x, y, Preview3DUtils.visibilityZ), color);
		}

		drawData.draw(GL33.GL_LINES);
	}
}
