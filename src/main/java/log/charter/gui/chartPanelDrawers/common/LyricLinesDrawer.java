package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.*;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.util.ScalingUtils.timeToX;

import java.awt.Font;
import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.song.vocals.Vocal;
import log.charter.util.Position2D;

public class LyricLinesDrawer {
	private static int height = lyricLinesY - beatSizeTextY;
	private static Font lyricLineFont = new Font(Font.DIALOG, Font.ITALIC, height - 3);

	public static void reloadGraphics() {
		height = lyricLinesY - beatSizeTextY;
		lyricLineFont = new Font(Font.DIALOG, Font.ITALIC, height - 3);
	}

	private static class VocalLinesDrawingData {
		private final DrawableShapeList backgrounds = new DrawableShapeList();
		private final DrawableShapeList texts = new DrawableShapeList();

		public void addLyricLine(final String text, final int x, final int lengthPx) {
			final ShapePositionWithSize backgroundPosition = new ShapePositionWithSize(x, lyricLinesY + 3, lengthPx,
					height);
			backgrounds.add(filledRectangle(backgroundPosition, ColorLabel.VOCAL_LINE_BACKGROUND.color()));

			final Position2D textPosition = new Position2D(x + 3, lyricLinesY + 6);
			texts.add(new Text(textPosition, lyricLineFont, text, ColorLabel.VOCAL_LINE_TEXT.color()));
		}

		public void draw(final Graphics g) {
			reloadGraphics();
			backgrounds.draw(g);
			texts.draw(g);
		}
	}

	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	public void draw(final Graphics g) {
		if (data == null || data.isEmpty) {
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
