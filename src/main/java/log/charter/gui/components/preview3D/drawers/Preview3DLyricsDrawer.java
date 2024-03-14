package log.charter.gui.components.preview3D.drawers;

import static log.charter.util.CollectionUtils.lastBefore;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.song.position.Position;
import log.charter.data.song.vocals.Vocal;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.util.CollectionUtils;

public class Preview3DLyricsDrawer {
	private ChartData chartData;
	private TextTexturesHolder textTexturesHolder;

	public void init(final ChartData chartData, final TextTexturesHolder textTexturesHolder) {
		this.chartData = chartData;
		this.textTexturesHolder = textTexturesHolder;
	}

	private double drawText(final ShadersHolder shadersHolder, final String text, final Color color, final double x0,
			final double y0, final double height, final double aspectRatio) {
		if (text == null || text.isEmpty()) {
			return 0;
		}

		final BufferedTextureData textureData = textTexturesHolder.setTextInTexture(text, 64f, color);

		final double x1 = x0 + height * textureData.width / textureData.height * aspectRatio;
		final double y1 = y0 - height;

		shadersHolder.new VideoShaderDrawData()//
				.addZQuad(x0, x1, y0, y1, 0, 1, 0, 1)//
				.draw(textTexturesHolder.getTextureId(), new Color(255, 255, 255, 255));

		return x1;
	}

	private int findLineStart(final List<Vocal> vocals, final int id) {
		for (int i = id - 1; i >= 0; i--) {
			if (vocals.get(i).isPhraseEnd()) {
				return i + 1;
			}
		}

		return 0;
	}

	private int findLineEnd(final List<Vocal> vocals, final int id) {
		for (int i = id; i < vocals.size(); i++) {
			if (vocals.get(i).isPhraseEnd()) {
				return i;
			}
		}

		return vocals.size() - 1;
	}

	private String getLineFromTo(final int startingId, final int endingId) {
		final List<Vocal> vocals = chartData.currentVocals().vocals;
		String text = "";
		for (int i = startingId; i <= endingId; i++) {
			final Vocal vocal = vocals.get(i);
			text += vocal.getText() + (vocal.isWordPart() ? "" : " ");
		}

		return text;
	}

	private void drawCurrentLine(final ShadersHolder shadersHolder, final int time, final double aspectRatio,
			final double textSizeMultiplier) {
		final List<Vocal> vocals = chartData.currentVocals().vocals;
		final Integer currentVocalId = lastBefore(vocals, new Position(time)).findId();
		if (currentVocalId == null) {
			return;
		}

		final int currentLineStart = findLineStart(vocals, currentVocalId);
		final int currentLineEnd = findLineEnd(vocals, currentVocalId);
		if (vocals.get(currentLineEnd).endPosition() < time - 100) {
			return;
		}

		final String textDone = getLineFromTo(currentLineStart, currentVocalId);
		final String textToDo = getLineFromTo(currentVocalId + 1, currentLineEnd);

		final double x1 = drawText(shadersHolder, textDone, ColorLabel.PREVIEW_3D_LYRICS_PASSED.color(), -0.6, 0.7,
				0.1 * textSizeMultiplier, aspectRatio);
		drawText(shadersHolder, textToDo, ColorLabel.PREVIEW_3D_LYRICS.color(), x1, 0.7, 0.1 * textSizeMultiplier,
				aspectRatio);
	}

	private void drawNextLine(final ShadersHolder shadersHolder, final int time, final double aspectRatio,
			final double textSizeMultiplier) {
		final List<Vocal> vocals = chartData.currentVocals().vocals;
		final Integer currentVocalId = CollectionUtils.lastBefore(vocals, new Position(time)).findId();
		if (currentVocalId == null) {
			return;
		}

		final int nextLineStart = findLineStart(vocals, findLineEnd(vocals, currentVocalId) + 1);
		final int nextLineEnd = findLineEnd(vocals, nextLineStart);
		final String textToDo = getLineFromTo(nextLineStart, nextLineEnd);
		drawText(shadersHolder, textToDo, ColorLabel.PREVIEW_3D_LYRICS.color(), -0.6, 0.7 - 0.14 * textSizeMultiplier,
				0.1 * textSizeMultiplier, aspectRatio);
	}

	public void draw(final ShadersHolder shadersHolder, final int time, final double aspectRatio,
			final double textSizeMultiplier) {
		GL30.glDisable(GL30.GL_DEPTH_TEST);

		drawCurrentLine(shadersHolder, time, aspectRatio, textSizeMultiplier);
		drawNextLine(shadersHolder, time, aspectRatio, textSizeMultiplier);

		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}
}
