package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.max;
import static log.charter.gui.ChartPanel.getLaneY;
import static log.charter.gui.ChartPanel.lyricLinesY;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Graphics;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.Drawer;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.FilledRectangle;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextDrawList;
import log.charter.io.rs.xml.vocals.Vocal;

public class VocalsDrawer implements Drawer {

	private void drawVocals(final Graphics g, final ChartPanel panel, final ChartData data) {
		final TextDrawList texts = new TextDrawList();
		final DrawableShapeList notes = new DrawableShapeList();
		final DrawableShapeList wordConnections = new DrawableShapeList();
		final DrawableShapeList phraseEnds = new DrawableShapeList();

		final List<Vocal> vocals = data.songChart.vocals.vocals;
		final int y = getLaneY(0, 1) - 3;
		final int w = panel.getWidth();

		for (int i = 0; i < vocals.size(); i++) {
			final Vocal vocal = vocals.get(i);
			final int x = timeToX(vocal.time, data.time);
			if (x > w) {
				break;
			}

			final int length = max(1, timeToXLength(vocal.length));
			if ((x + length) > 0) {
				(vocal.isPhraseEnd() ? phraseEnds : notes).add(new FilledRectangle(x, y, length, 8));
			}

			final String text = vocal.getText();
			if ((x + g.getFontMetrics().stringWidth(text)) > 0) {
				texts.addString(text, x, ChartPanel.textY + 17);
			}
			if (vocal.isWordPart() && (i < (vocals.size() - 1))) {
				final Vocal next = vocals.get(i + 1);
				final int nextStart = timeToX(next.time, data.time);
				wordConnections.add(new FilledRectangle(x + length, y + 2, nextStart - x - length, 4));
			}
		}

		g.setColor(ChartPanelColors.get(ColorLabel.LYRIC_TEXT));
		texts.draw(g, ChartPanelColors.get(ColorLabel.LYRIC_TEXT));
		g.setColor(ChartPanelColors.get(ColorLabel.LYRIC_WORD_PART));
		wordConnections.draw(g);
		g.setColor(ChartPanelColors.get(ColorLabel.LYRIC));
		notes.draw(g);
		g.setColor(ChartPanelColors.get(ColorLabel.LYRIC_PHRASE_END));
		phraseEnds.draw(g);
	}

	private void drawLyricLines(final Graphics g, final ChartPanel panel, final ChartData data) {
		final TextDrawList lyricLines = new TextDrawList();
		final DrawableShapeList lines = new DrawableShapeList();
		String currentLine = "";
		boolean started = false;
		int x = 0;

		for (final Vocal vocal : data.songChart.vocals.vocals) {
			if (!started) {
				started = true;
				x = timeToX(vocal.time, data.time);
			}

			currentLine += vocal.getText();
			if (currentLine.endsWith("-")) {
				currentLine = currentLine.substring(0, currentLine.length() - 1);
			} else {
				currentLine += " ";
			}

			if (vocal.isPhraseEnd()) {
				lines.add(new FilledRectangle(x, lyricLinesY, timeToXLength(vocal.length), 13));
				lyricLines.addString(currentLine, x, lyricLinesY + 11);
			}
		}

		g.setColor(ChartPanelColors.get(ColorLabel.LYRIC_LINE));
		lines.draw(g);
		lyricLines.draw(g, ChartPanelColors.get(ColorLabel.LYRIC_LINE_TEXT));
	}

	private void drawSelectedVocals(final Graphics g, final ChartPanel panel, final ChartData data) {
//		final DrawList selects = new DrawList();
//		final int w = panel.getWidth();
//
//		for (final int id : data.selectedNotes) {
//			final Lyric l = data.s.v.lyrics.get(id);
//			final int x = data.timeToX(l.pos);
//			final int y = getLaneY(0, 1) - 4;
//			int length = data.timeToXLength(l.getLength()) + 1;
//			if (length < 3) {
//				length = 3;
//			}
//			if (x > (w + (ChartPanel.noteW / 2))) {
//				break;
//			}
//			if ((x + length) > 0) {
//				for (int c = 0; c < 5; c++) {
//					selects.addPositions(x - 1, y, length, 9);
//				}
//			}
//		}
//
//		selects.draw(g, ChartPanel.colors.get("SELECT"));
	}
//
//	private void drawDebugNoteId(final Graphics g, final ChartPanel panel, final ChartData data) {
//		for (int i = 0; i < data.currentNotes.size(); i++) {
//			final Note n = data.currentNotes.get(i);
//			final int x = data.timeToX(n.pos);
//			if (x >= 0 && x < panel.getWidth()) {
//				g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
//				g.drawString("" + i, x - 5, ChartPanel.beatTextY - 10);
//			}
//		}
//	}

	@Override
	public void draw(final Graphics g, final ChartPanel panel, final ChartData data) {
		if (data.isEmpty) {
			return;
		}

		drawLyricLines(g, panel, data);
		drawVocals(g, panel, data);

		if (data.drawDebug) {
			// drawDebugNoteId(g, panel, data);
		}
	}
}
