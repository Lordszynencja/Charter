package log.charter.gui.components.preview3D.drawers;

import static log.charter.data.config.Config.frets;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.stringDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.topStringPosition;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import log.charter.data.ChartData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.TexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseTextureShaderDrawData;

public class Preview3DInlayDrawer {
	private ChartData data;
	private TexturesHolder texturesHolder;

	public void init(final ChartData data, final TexturesHolder texturesHolder) {
		this.data = data;
		this.texturesHolder = texturesHolder;
	}

	public void draw(final ShadersHolder shadersHolder) {
		final BaseTextureShaderDrawData drawData = shadersHolder.new BaseTextureShaderDrawData();

		final double y0 = topStringPosition + stringDistance / 2;
		final double y1 = topStringPosition - stringDistance * (data.currentStrings() - 0.5);

		for (int i = 0; i <= frets && i < 32; i++) {
			final double x0 = getFretPosition(i);
			final double x1 = getFretPosition(i + 1);
			final double tx0 = (i % 8) / 8.0;
			final double tx1 = tx0 + 0.125;
			final double ty0 = (i / 8) / 4.0;
			final double ty1 = ty0 + 0.25;

			drawData.addZQuad(x0, x1, y0, y1, -0.01, tx0, tx1, ty0, ty1);
		}

		GL30.glDisable(GL30.GL_DEPTH_TEST);
		drawData.draw(GL33.GL_QUADS, Matrix4.identity, texturesHolder.getTextureId("inlay"));
		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
}
