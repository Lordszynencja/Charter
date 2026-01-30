package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static log.charter.gui.components.preview3D.Preview3DUtils.getStringPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.HandShapeDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.util.data.IntRange;

public class Preview3DChordNamesDrawer {
	private static final double chordNameSize = 0.7;

	private ChartData chartData;
	private TextTexturesHolder textTexturesHolder;

	public void init(final ChartData chartData, final TextTexturesHolder textTexturesHolder) {
		this.chartData = chartData;
		this.textTexturesHolder = textTexturesHolder;
	}

	private void addChordName(final ShadersHolder shadersHolder, final Preview3DDrawData drawData,
			final HandShapeDrawData handShape) {
		if (handShape.timeFrom <= drawData.time && handShape.timeTo <= drawData.time + 150) {
			return;
		}

		final double timeFrom = max(0, handShape.timeFrom - drawData.time);
		final IntRange frets = drawData.getFrets(handShape.originalPosition);
		if (frets == null) {
			return;
		}

		final double z = getTimePosition(timeFrom);

		final String chordName = handShape.template.chordName;
		if (chordName == null || chordName.isBlank()) {
			return;
		}

		final BufferedTextureData textureData = textTexturesHolder.setTextInTexture(handShape.template.chordName, 64f,
				ColorLabel.PREVIEW_3D_CHORD_NAME.color(), true);

		final double x0 = frets.min - 1.75;
		final double x1 = x0 + chordNameSize * textureData.width / textureData.height;
		final double y1 = getStringPosition(0, chartData.currentStrings()) + 0.5;
		final double y0 = y1 + chordNameSize;

		shadersHolder.new BaseTextureShaderDrawData()//
				.addZQuad(x0, x1, y0, y1, z, 0, 1, 0, 1)//
				.draw(GL30.GL_QUADS, Matrix4.identity, textTexturesHolder.getTextureId());
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		for (int i = drawData.handShapes.size() - 1; i >= 0; i--) {
			addChordName(shadersHolder, drawData, drawData.handShapes.get(i));
		}
	}
}
