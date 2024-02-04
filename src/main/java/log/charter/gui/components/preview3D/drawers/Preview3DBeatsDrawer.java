package log.charter.gui.components.preview3D.drawers;

import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretMiddlePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.visibility;
import static log.charter.gui.components.preview3D.data.BeatDrawData.getBeatsForTimeSpanWithRepeats;
import static log.charter.util.ColorUtils.setAlpha;
import static log.charter.util.ColorUtils.transparent;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.BeatDrawData;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.FadingShaderDrawData;

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

		Color color = Color.WHITE;
		if (z < closeDistanceZ) {
			color = setAlpha(color, (int) (255 * z / closeDistanceZ));
		}

		final BufferedTextureData textureData = textTexturesHolder.setTextInTexture("" + fret, 128f, color);

		final double x = getFretMiddlePosition(fret);
		final double width = (getFretPosition(fret) - getFretPosition(fret - 1)) * textureData.width
				/ textureData.height;
		final double x0 = x - width * 0.4;
		final double x1 = x + width * 0.4;
		final double y0 = y - 0.1;
		final double y1 = y - 1;

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

	private void addQuad(final FadingShaderDrawData drawData, final double x0, final double x1, final double y,
			final double z0, final double z1, final Color color0, final Color color1) {
		drawData.addVertex(new Point3D(x0, y, z0), color0)//
				.addVertex(new Point3D(x1, y, z0), color0)//
				.addVertex(new Point3D(x1, y, z1), color1)//
				.addVertex(new Point3D(x0, y, z1), color1);
	}

	private void drawBeats(final ShadersHolder shadersHolder, final List<BeatDrawData> drawnBeatsData) {
		final FadingShaderDrawData drawData = shadersHolder.new FadingShaderDrawData();
		final FadingShaderDrawData lineDrawData = shadersHolder.new FadingShaderDrawData();

		final double y = getChartboardYPosition(data.currentStrings()) + 0.0001;
		final Color color = ColorLabel.PREVIEW_3D_BEAT.color();
		final Color alpha = transparent(color);

		for (final BeatDrawData beat : drawnBeatsData) {
			final double x0 = getFretPosition(beat.fretFrom);
			final double x1 = getFretPosition(beat.fretTo);
			final double z = getTimePosition(beat.time - data.time);

			lineDrawData.addVertex(new Point3D(x0, y, z), color)//
					.addVertex(new Point3D(x1, y, z), color);
			if (beat.firstInMeasure) {
				addQuad(drawData, x0, x1, y, z - 0.2, z - 0.1, alpha, color);
				addQuad(drawData, x0, x1, y, z - 0.1, z + 0.1, color, color);
				addQuad(drawData, x0, x1, y, z + 0.1, z + 0.2, color, alpha);
			} else {
				addQuad(drawData, x0, x1, y, z - 0.1, z, alpha, color);
				addQuad(drawData, x0, x1, y, z, z + 0.1, color, alpha);
			}
		}

		GL30.glLineWidth(1f);
		lineDrawData.draw(GL30.GL_LINES, Matrix4.identity, closeDistanceZ, 0);
		drawData.draw(GL30.GL_QUADS, Matrix4.identity, closeDistanceZ, 0);
	}

	public void draw(final ShadersHolder shadersHolder) {
		final List<BeatDrawData> drawnBeatsData = getBeatsForTimeSpanWithRepeats(data, repeatManager, data.time,
				data.time + visibility);

		drawFretNumbers(shadersHolder, drawnBeatsData);
		drawBeats(shadersHolder, drawnBeatsData);
	}
}
