package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistance;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.util.ColorUtils.setAlpha;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.Preview3DUtils;
import log.charter.gui.components.preview3D.data.BeatDrawData;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.BaseShaderDrawData;
import log.charter.util.ColorUtils;

public class Preview3DBeatsDrawer {

	private ChartData data;
	private RepeatManager repeatManager;
	private TextTexturesHolder textTexturesHolder;

	public Matrix4 currentMatrix;

	public void init(final ChartData data, final RepeatManager repeatManager,
			final TextTexturesHolder textTexturesHolder) {
		this.data = data;
		this.repeatManager = repeatManager;
		this.textTexturesHolder = textTexturesHolder;
	}

	private void drawFretNumber(final ShadersHolder shadersHolder, final int fret, final double y, final double z) {
		if (fret > Config.frets) {
			return;
		}

		final BufferedTextureData textureData = textTexturesHolder.setTextInTexture("" + fret, 128f, Color.WHITE);

		final double x = Preview3DUtils.getFretMiddlePosition(fret);
		final double x0 = x - 0.04;
		final double x1 = x + 0.04;

		final double heightMultiplier = 2 * (x1 - x0) * textureData.height / textureData.width;
		final double y0 = y - heightMultiplier * 0.1;
		final double y1 = y - heightMultiplier * 2;

		shadersHolder.new BaseTextureShaderDrawData()//
				.addZQuad(x0, x1, y0, y1, z, 0, 1, 0, 1)//
				.draw(GL30.GL_QUADS, Matrix4.identity, textTexturesHolder.getTextureId());
	}

	private void drawFretNumbers(final ShadersHolder shadersHolder, final List<BeatDrawData> drawnBeatsData) {
		final double y = getChartboardYPosition(data.currentStrings());

		for (final BeatDrawData beat : drawnBeatsData) {
			if (!beat.firstInMeasure) {
				continue;
			}

			final double z = getTimePosition(beat.time - data.time);

			for (int fret = 0; fret < Config.frets; fret += 12) {
				drawFretNumber(shadersHolder, fret + 3, y, z);
				drawFretNumber(shadersHolder, fret + 5, y, z);
				drawFretNumber(shadersHolder, fret + 7, y, z);
				drawFretNumber(shadersHolder, fret + 9, y, z);
				drawFretNumber(shadersHolder, fret + 12, y, z);
			}
		}
	}

	private void drawBeats(final ShadersHolder shadersHolder, final List<BeatDrawData> drawnBeatsData) {
		final BaseShaderDrawData drawData = shadersHolder.new BaseShaderDrawData();

		final double x0 = getFretPosition(0);
		final double x1 = getFretPosition(Config.frets);
		final double y = getChartboardYPosition(data.currentStrings()) + 0.0001;
		final Color color = ColorLabel.PREVIEW_3D_BEAT.color();
		final Color alpha = ColorUtils.transparent(color);

		for (final BeatDrawData beat : drawnBeatsData) {
			final int beatTime = beat.time - data.time;
			final Color beatColor = beatTime > closeDistance ? color
					: setAlpha(color, max(0, min(255, 255 * beatTime / closeDistance)));

			final double z = getTimePosition(beat.time - data.time);

			if (beat.firstInMeasure) {
				drawData.addVertex(new Point3D(x0, y, z - 0.2), alpha)//
						.addVertex(new Point3D(x1, y, z - 0.2), alpha)//
						.addVertex(new Point3D(x1, y, z - 0.1), beatColor)//
						.addVertex(new Point3D(x0, y, z - 0.1), beatColor)//

						.addVertex(new Point3D(x0, y, z - 0.1), beatColor)//
						.addVertex(new Point3D(x1, y, z - 0.1), beatColor)//
						.addVertex(new Point3D(x1, y, z + 0.1), beatColor)//
						.addVertex(new Point3D(x0, y, z + 0.1), beatColor)//

						.addVertex(new Point3D(x0, y, z + 0.1), beatColor)//
						.addVertex(new Point3D(x1, y, z + 0.1), beatColor)//
						.addVertex(new Point3D(x1, y, z + 0.2), alpha)//
						.addVertex(new Point3D(x0, y, z + 0.2), alpha);
			} else {
				drawData.addVertex(new Point3D(x0, y, z - 0.1), alpha)//
						.addVertex(new Point3D(x1, y, z - 0.1), alpha)//
						.addVertex(new Point3D(x1, y, z), beatColor)//
						.addVertex(new Point3D(x0, y, z), beatColor)//

						.addVertex(new Point3D(x0, y, z), beatColor)//
						.addVertex(new Point3D(x1, y, z), beatColor)//
						.addVertex(new Point3D(x1, y, z + 0.1), alpha)//
						.addVertex(new Point3D(x0, y, z + 0.1), alpha);
			}
		}

		drawData.draw(GL30.GL_QUADS, Matrix4.identity);
	}

	public void draw(final ShadersHolder shadersHolder) {
		final List<BeatDrawData> drawnBeatsData = getBeatsForTimeSpanWithRepeats(data, repeatManager, data.time,
				data.time + visibility);

		drawFretNumbers(shadersHolder, drawnBeatsData);
		drawBeats(shadersHolder, drawnBeatsData);
	}
}
