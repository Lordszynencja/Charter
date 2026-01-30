package log.charter.gui.components.preview3D.drawers;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL30;

import log.charter.data.ChartData;
import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.song.EventPoint;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.Position;
import log.charter.gui.components.preview3D.glUtils.BufferedTextureData;
import log.charter.gui.components.preview3D.glUtils.TextTexturesHolder;
import log.charter.gui.components.preview3D.shaders.ShadersHolder;
import log.charter.util.CollectionUtils;

public class Preview3DSectionDrawer {
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

	private EventPoint findCurrentPhrase(final IConstantFractionalPosition timeAsFraction) {
		final List<EventPoint> phrases = chartData.currentArrangement().getFilteredEventPoints(EventPoint::hasPhrase);
		return CollectionUtils.lastBeforeEqual(phrases, timeAsFraction).find();
	}

	private EventPoint findCurrentSection(final IConstantFractionalPosition timeAsFraction) {
		final List<EventPoint> sections = chartData.currentArrangement()
				.getFilteredEventPoints(ep -> ep.section != null);
		return CollectionUtils.lastBeforeEqual(sections, timeAsFraction).find();
	}

	public void draw(final ShadersHolder shadersHolder, final double time, final double aspectRatio) {
		if (time < chartData.beats().get(0).position()) {
			return;
		}

		GL30.glDisable(GL30.GL_DEPTH_TEST);

		final IConstantFractionalPosition timeAsFraction = new Position(time).toFraction(chartData.beats());
		final EventPoint currentPhrase = findCurrentPhrase(timeAsFraction);
		if (currentPhrase != null && !"END".equals(currentPhrase.phrase)) {
			drawText(shadersHolder, currentPhrase.phrase, ColorLabel.PHRASE_COLOR.color(), -0.6, 0.83, 0.08,
					aspectRatio);
			final EventPoint currentSection = findCurrentSection(timeAsFraction);
			if (currentSection != null) {
				drawText(shadersHolder, currentSection.section.label.label(), ColorLabel.SECTION_COLOR.color(), -0.6,
						0.95, 0.13, aspectRatio);
			}
		}

		GL30.glEnable(GL30.GL_DEPTH_TEST);
	}

}
