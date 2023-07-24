package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lyricLinesY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.util.ScalingUtils.timeToX;

import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.song.vocals.Vocal;
import log.charter.util.Position2D;

public class LyricLinesDrawer {
	private static class VocalLinesDrawingData {
		private final DrawableShapeList backgrounds = new DrawableShapeList();
		private final DrawableShapeList texts = new DrawableShapeList();

		public void addLyricLine(final String text, final int x, final int lengthPx) {
			final ShapePositionWithSize backgroundPosition = new ShapePositionWithSize(x, lyricLinesY - 4, lengthPx,
					19);
			backgrounds.add(filledRectangle(backgroundPosition, ColorLabel.VOCAL_LINE_BACKGROUND.color()));
			final Position2D textPosition = new Position2D(x + 3, lyricLinesY + 11);
			texts.add(text(textPosition, text, ColorLabel.VOCAL_LINE_TEXT.color()));
		}

		public void draw(final Graphics g) {
			backgrounds.draw(g);
			texts.draw(g);
		}
	}

	private boolean initiated;

	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;

		initiated = true;
	}

	public void draw(final Graphics g) {
		if (!initiated || data.isEmpty) {
			return;
		}

		final VocalLinesDrawingData drawingData = new VocalLinesDrawingData();
		String currentLine = "";
		boolean started = false;
		int x = 0;

		for (final Vocal vocal : data.songChart.vocals.vocals) {
			if (!started) {
				started = true;
				x = timeToX(vocal.position(), data.time);
			}

			currentLine += vocal.getText();
			if (!vocal.isWordPart()) {
				currentLine += " ";
			}

			if (vocal.isPhraseEnd()) {
				drawingData.addLyricLine(currentLine, x, timeToX(vocal.position() + vocal.length(), data.time) - x);
				currentLine = "";
				started = false;
			}
		}

		drawingData.draw(g);
	}
}
