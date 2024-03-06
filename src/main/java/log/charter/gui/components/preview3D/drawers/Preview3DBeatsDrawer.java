package log.charter.gui.components.preview3D.drawers;

import static java.lang.Math.max;
import static log.charter.gui.components.preview3D.Preview3DUtils.closeDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.fadedDistanceZ;
import static log.charter.gui.components.preview3D.Preview3DUtils.getChartboardYPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretMiddlePosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getFretPosition;
import static log.charter.gui.components.preview3D.Preview3DUtils.getTimePosition;
import static log.charter.util.ColorUtils.setAlpha;
import static log.charter.util.ColorUtils.transparent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.data.AnchorDrawData;
import log.charter.gui.components.preview3D.data.BeatDrawData;
import log.charter.gui.components.preview3D.data.Preview3DDrawData;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.Matrix4;
import log.charter.gui.components.preview3D.glUtils.Point3D;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder.FadingShaderDrawData;
import log.charter.util.IntRange;

public class Preview3DBeatsDrawer {
	private static final int[] dottedFretDistances = { 3, 2, 2, 2, 3 };

	private ChartData data;
	private TextTexturesHolder textTexturesHolder;

	public Matrix4 currentMatrix;

	public void init(final ChartData data, final TextTexturesHolder textTexturesHolder) {
		this.data = data;
		this.textTexturesHolder = textTexturesHolder;
	}

	private void addQuad(final FadingShaderDrawData drawData, final double x0, final double x1, final double y,
			final double z0, final double z1, final Color color0, final Color color1) {
		drawData.addVertex(new Point3D(x0, y, z0), color0)//
				.addVertex(new Point3D(x1, y, z0), color0)//
				.addVertex(new Point3D(x1, y, z1), color1)//
				.addVertex(new Point3D(x0, y, z1), color1);
	}

	private void drawBeats(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final FadingShaderDrawData shaderDrawData = shadersHolder.new FadingShaderDrawData();
		final FadingShaderDrawData shaderLineDrawData = shadersHolder.new FadingShaderDrawData();

		final double y = getChartboardYPosition(data.currentStrings()) + 0.0001;
		final Color color = ColorLabel.PREVIEW_3D_BEAT.color();
		final Color alpha = transparent(color);

		for (final BeatDrawData beat : drawData.beats) {
			final IntRange frets = drawData.getFrets(beat.originalTime);
			final double x0 = getFretPosition(frets.min - 1);
			final double x1 = getFretPosition(frets.max);
			final double z = getTimePosition(beat.time - drawData.time);

			shaderLineDrawData.addVertex(new Point3D(x0, y, z), color)//
					.addVertex(new Point3D(x1, y, z), color);
			if (beat.firstInMeasure) {
				addQuad(shaderDrawData, x0, x1, y, z - 0.2, z - 0.1, alpha, color);
				addQuad(shaderDrawData, x0, x1, y, z - 0.1, z + 0.1, color, color);
				addQuad(shaderDrawData, x0, x1, y, z + 0.1, z + 0.2, color, alpha);
			} else {
				addQuad(shaderDrawData, x0, x1, y, z - 0.1, z, alpha, color);
				addQuad(shaderDrawData, x0, x1, y, z, z + 0.1, color, alpha);
			}
		}

		GL30.glLineWidth(1f);
		shaderLineDrawData.draw(GL30.GL_LINES, Matrix4.identity, closeDistanceZ, fadedDistanceZ);
		shaderDrawData.draw(GL30.GL_QUADS, Matrix4.identity, closeDistanceZ, fadedDistanceZ);
	}

	private void drawFretNumberWithFade(final ShadersHolder shadersHolder, final int fret, final double y,
			final double z, Color color) {
		if (fret > Config.frets) {
			return;
		}

		if (z < closeDistanceZ) {
			color = setAlpha(color,
					max(0, (int) (color.getAlpha() * (z - fadedDistanceZ) / (closeDistanceZ - fadedDistanceZ))));
		}

		drawFretNumber(shadersHolder, fret, y, z, color);
	}

	private void drawFretNumber(final ShadersHolder shadersHolder, final int fret, final double y, final double z,
			final Color color) {
		if (fret > Config.frets) {
			return;
		}

		final BufferedTextureData textureData = textTexturesHolder.setTextInTexture("" + fret, 128f, color);

		final double x = getFretMiddlePosition(fret);
		final double width = (getFretPosition(fret) - getFretPosition(fret - 1)) * textureData.width
				/ textureData.height;
		final double x0 = x - width * 0.4;
		final double x1 = x + width * 0.4;
		final double y0 = y + 0.5;
		final double y1 = y - 0.5;

		shadersHolder.new BaseTextureShaderDrawData()//
				.addZQuad(x0, x1, y0, y1, z, 0, 1, 0, 1)//
				.draw(GL30.GL_QUADS, Matrix4.identity, textTexturesHolder.getTextureId());
	}

	private static class FretDrawData implements Comparable<FretDrawData> {
		public final int position;
		public final int fret;
		public final boolean fromAnchor;
		public final boolean active;

		public FretDrawData(final int position, final int fret, final boolean fromAnchor, final boolean active) {
			this.position = position;
			this.fret = fret;
			this.fromAnchor = fromAnchor;
			this.active = active;
		}

		@Override
		public int compareTo(final FretDrawData o) {
			final int positionDifference = Integer.compare(position, o.position);
			if (positionDifference != 0) {
				return positionDifference;
			}

			return fromAnchor == o.fromAnchor ? 0 : fromAnchor ? 1 : -1;
		}
	}

	private void addFretsToDrawForMeasures(final Preview3DDrawData drawData, final List<FretDrawData> fretsToDraw) {
		for (final BeatDrawData beat : drawData.beats) {
			if (!beat.firstInMeasure) {
				continue;
			}

			final IntRange fretsRange = drawData.getFrets(beat.originalTime);
			int fret = 0;
			int i = 0;
			while (fret <= Config.frets) {
				fret += dottedFretDistances[i = (i + 1) % dottedFretDistances.length];
				fretsToDraw.add(
						new FretDrawData(beat.time, fret, false, fret >= fretsRange.min && fret <= fretsRange.max));
			}
		}
	}

	private void drawActiveFretNumbers(final ShadersHolder shadersHolder, final IntRange currentFrets, final double y) {
		final Color anchorFretColor = ColorLabel.PREVIEW_3D_ANCHOR_FRET_COLOR.color();
		for (int fret = currentFrets.min; fret <= currentFrets.max; fret++) {
			final double z = getTimePosition(0);
			drawFretNumber(shadersHolder, fret, y, z, anchorFretColor);
		}
	}

	private void drawFretNumbers(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		final double y = getChartboardYPosition(data.currentStrings());

		final List<FretDrawData> fretsToDraw = new ArrayList<>(100);

		addFretsToDrawForMeasures(drawData, fretsToDraw);

		final IntRange currentFrets = drawData.getFrets(drawData.time);
		for (final AnchorDrawData anchor : drawData.anchors) {
			if (anchor.timeFrom > drawData.time) {
				fretsToDraw.add(new FretDrawData(anchor.timeFrom, anchor.fretFrom + 1, true, true));
			}
		}

		fretsToDraw.sort(null);

		final Color beatActiveFretColor = ColorLabel.PREVIEW_3D_BEAT_NUMBER_ACTIVE_COLOR.color();
		final Color beatFretColor = setAlpha(ColorLabel.PREVIEW_3D_LANE_BORDER.color(), 128);
		final Color anchorFretColor = ColorLabel.PREVIEW_3D_ANCHOR_FRET_COLOR.color();

		fretsToDraw.forEach(fretToDraw -> {
			final double z = getTimePosition(fretToDraw.position - drawData.time);
			final Color color = fretToDraw.fromAnchor ? anchorFretColor //
					: fretToDraw.active ? beatActiveFretColor : beatFretColor;
			drawFretNumberWithFade(shadersHolder, fretToDraw.fret, y, z, color);
		});

		drawActiveFretNumbers(shadersHolder, currentFrets, y);
	}

	public void draw(final ShadersHolder shadersHolder, final Preview3DDrawData drawData) {
		drawBeats(shadersHolder, drawData);

		GL30.glDisable(GL30.GL_DEPTH_TEST);
		drawFretNumbers(shadersHolder, drawData);
		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
}
