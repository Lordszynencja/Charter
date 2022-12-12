package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.max;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.SelectionManager;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePosition;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class VocalsDrawer {
	private final static Color selectColor = ChartPanelColors.get(ColorLabel.SELECT);
	private final static Color vocalTextColor = ChartPanelColors.get(ColorLabel.VOCAL_TEXT);
	private final static Color vocalNoteColor = ChartPanelColors.get(ColorLabel.VOCAL_NOTE);
	private final static Color vocalNotePhraseEndColor = ChartPanelColors.get(ColorLabel.VOCAL_NOTE_PHRASE_END);
	private final static Color vocalNoteWordPartColor = ChartPanelColors.get(ColorLabel.VOCAL_NOTE_WORD_PART);

	private static final int vocalNoteY = (lanesTop + lanesBottom) / 2;

	private static ShapePositionWithSize getVocalNotePosition(final int x, final int length) {
		return new ShapePositionWithSize(x, vocalNoteY - 4, length, 8);
	}

	public static ShapePositionWithSize getVocalNotePosition(final int position, final int length, final int time) {
		return getVocalNotePosition(timeToX(position, time), timeToXLength(length));
	}

	private class VocalNotesDrawingData {
		private final DrawableShapeList texts = new DrawableShapeList();
		private final DrawableShapeList notes = new DrawableShapeList();
		private final DrawableShapeList wordConnections = new DrawableShapeList();

		private final FontMetrics fontMetrics;
		private final int time;

		public VocalNotesDrawingData(final FontMetrics fontMetrics, final int time) {
			this.fontMetrics = fontMetrics;
			this.time = time;
		}

		public void addVocal(final Vocal vocal, final Vocal next, final int x, final int lengthPx,
				final boolean selected) {
			if ((x + lengthPx) > 0) {
				final Color color = vocal.isPhraseEnd() ? vocalNotePhraseEndColor : vocalNoteColor;
				final ShapePositionWithSize position = getVocalNotePosition(x, lengthPx);
				notes.add(filledRectangle(position, color));
				if (selected) {
					notes.add(strokedRectangle(position.resized(-1, -1, 1, 1), selectColor));
				}

				final String text = vocal.getText() + (vocal.isWordPart() ? "-" : "");
				if ((x + fontMetrics.stringWidth(text)) > 0) {
					texts.add(text(new ShapePosition(x + 2, vocalNoteY - 10), text, vocalTextColor));
				}
			}

			if (vocal.isWordPart() && next != null) {
				final int nextStart = timeToX(next.position, time);
				final ShapePositionWithSize position = new ShapePositionWithSize(x + lengthPx, vocalNoteY,
						nextStart - x - lengthPx, 4)//
								.centeredY();
				wordConnections.add(filledRectangle(position, vocalNoteWordPartColor));
			}
		}

		public void draw(final Graphics g) {
			wordConnections.draw(g);
			notes.draw(g);
			texts.draw(g);
		}
	}

	private boolean initiated = false;

	private ChartData data;
	private ChartPanel chartPanel;
	private SelectionManager selectionManager;

	private final AudioDrawer audioDrawer = new AudioDrawer();
	private final BeatsDrawer beatsDrawer = new BeatsDrawer();

	public void init(final ChartData data, final ChartPanel chartPanel, final SelectionManager selectionManager) {
		this.data = data;
		this.chartPanel = chartPanel;
		this.selectionManager = selectionManager;

		audioDrawer.init(data, chartPanel);
		beatsDrawer.init(data, chartPanel);

		initiated = true;
	}

	private void drawVocals(final Graphics g) {
		final VocalNotesDrawingData drawingData = new VocalNotesDrawingData(g.getFontMetrics(), data.time);

		final ArrayList2<Vocal> vocals = data.songChart.vocals.vocals;
		final int width = chartPanel.getWidth();
		final HashSet2<Integer> selectedVocalIds = selectionManager.getSelectedVocalsSet()
				.map(selection -> selection.id);

		for (int i = 0; i < vocals.size(); i++) {
			final Vocal vocal = vocals.get(i);
			final int x = timeToX(vocal.position, data.time);
			if (x > width) {
				break;
			}

			final Vocal next = vocals.size() > i + 1 ? vocals.get(i + 1) : null;
			final int length = max(1, timeToXLength(vocal.length));
			final boolean isSelected = selectedVocalIds.contains(i);
			drawingData.addVocal(vocal, next, x, length, isSelected);
		}

		drawingData.draw(g);
	}

	private static class VocalLinesDrawingData {
		private final static Color vocalLineBackgroundColor = ChartPanelColors.get(ColorLabel.VOCAL_LINE_BACKGROUND);
		private final static Color vocalLineTextColor = ChartPanelColors.get(ColorLabel.VOCAL_LINE_TEXT);

		public static final int lyricLinesY = 30;

		private final DrawableShapeList backgrounds = new DrawableShapeList();
		private final DrawableShapeList texts = new DrawableShapeList();

		public void addLyricLine(final String text, final int x, final int lengthPx) {
			final ShapePositionWithSize backgroundPosition = new ShapePositionWithSize(x, lyricLinesY - 4, lengthPx,
					19);
			backgrounds.add(filledRectangle(backgroundPosition, vocalLineBackgroundColor));
			final ShapePosition textPosition = new ShapePosition(x + 3, lyricLinesY + 11);
			texts.add(text(textPosition, text, vocalLineTextColor));
		}

		public void draw(final Graphics g) {
			backgrounds.draw(g);
			texts.draw(g);
		}
	}

	private void drawLyricLines(final Graphics g) {
		final VocalLinesDrawingData drawingData = new VocalLinesDrawingData();
		String currentLine = "";
		boolean started = false;
		int x = 0;

		for (final Vocal vocal : data.songChart.vocals.vocals) {
			if (!started) {
				started = true;
				x = timeToX(vocal.position, data.time);
			}

			currentLine += vocal.getText();
			if (!vocal.isWordPart()) {
				currentLine += " ";
			}

			if (vocal.isPhraseEnd()) {
				drawingData.addLyricLine(currentLine, x, timeToX(vocal.position + vocal.length, data.time) - x);
				currentLine = "";
				started = false;
			}
		}

		drawingData.draw(g);
	}

	private void drawSelectedVocals(final Graphics g) {
		selectionManager.getSelectedVocals();
		// TODO make after selection is done
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

	public void draw(final Graphics g) {
		if (!initiated || data.isEmpty) {
			return;
		}

		audioDrawer.draw(g);
		beatsDrawer.draw(g);
		drawVocals(g);
		drawLyricLines(g);
		drawSelectedVocals(g);

		if (data.drawDebug) {
			// drawDebugNoteId(g, panel, data);
		}
	}
}
