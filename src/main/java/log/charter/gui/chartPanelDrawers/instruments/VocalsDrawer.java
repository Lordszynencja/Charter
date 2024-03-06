package log.charter.gui.chartPanelDrawers.instruments;

import static java.lang.Math.max;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Font;
import java.awt.Graphics;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;

public class VocalsDrawer {
	private static final Font vocalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
	private static int vocalNoteY = (lanesTop + lanesBottom) / 2;

	public static void reloadGraphics() {
		vocalNoteY = (lanesTop + lanesBottom) / 2;
	}

	private static ShapePositionWithSize getVocalNotePosition(final int x, final int length) {
		return new ShapePositionWithSize(x, vocalNoteY - 4, length, 8);
	}

	public static ShapePositionWithSize getVocalNotePosition(final int position, final int length, final int time) {
		final int x0 = timeToX(position, time);
		final int x1 = timeToX(position + length, time);
		return getVocalNotePosition(x0, x1 - x0);
	}

	private class VocalNotesDrawingData {
		private final DrawableShapeList texts = new DrawableShapeList();
		private final DrawableShapeList notes = new DrawableShapeList();
		private final DrawableShapeList wordConnections = new DrawableShapeList();

		private final Graphics g;
		private final int time;

		public VocalNotesDrawingData(final Graphics g, final int time) {
			this.g = g;
			this.time = time;
		}

		private void addVocal(final Vocal vocal, final int x, final int length, final boolean selected) {
			final ShapePositionWithSize positionAndSize = getVocalNotePosition(x, length);
			notes.add(filledRectangle(positionAndSize, ColorLabel.VOCAL_NOTE, true));
			if (selected) {
				notes.add(strokedRectangle(positionAndSize.resized(-1, -1, 1, 1), ColorLabel.VOCAL_SELECT));
			}

			final String text = vocal.getText() + (vocal.isWordPart() ? "-" : "");
			final ShapeSize expectedTextSize = Text.getExpectedSize(g, vocalFont, text);
			if (x + expectedTextSize.width <= 0) {
				return;
			}

			final int textY = vocalNoteY - expectedTextSize.height - positionAndSize.height;
			texts.add(new Text(new Position2D(x + 2, textY), vocalFont, text, ColorLabel.VOCAL_TEXT));
		}

		private void addConnection(final int x, final Vocal next) {
			final int nextStart = timeToX(next.position(), time);
			final ShapePositionWithSize position = new ShapePositionWithSize(x, vocalNoteY, nextStart - x, 4)//
					.centeredY();
			wordConnections.add(filledRectangle(position, ColorLabel.VOCAL_NOTE.colorWithAlpha(192)));
		}

		public void addVocal(final Vocal vocal, final Vocal next, final int x, final int length,
				final boolean selected) {
			final int x1 = x + length;
			if (x1 > 0) {
				addVocal(vocal, x, length, selected);
			}

			if (vocal.isWordPart() && next != null) {
				addConnection(x1, next);
			}
		}

		public void addHighlight(final int time, final int position, final int length) {
			final ShapePositionWithSize positionAndSize = getVocalNotePosition(position, length, time);
			notes.add(strokedRectangle(positionAndSize.resized(-1, -1, 1, 1), ColorLabel.HIGHLIGHT));
		}

		public void draw(final Graphics g) {
			wordConnections.draw(g);
			notes.draw(g);
			texts.draw(g);
		}
	}

	private BeatsDrawer beatsDrawer;
	private ChartData data;
	private ChartPanel chartPanel;
	private LyricLinesDrawer lyricLinesDrawer;
	private SelectionManager selectionManager;
	private WaveFormDrawer waveFormDrawer;

	public void init(final BeatsDrawer beatsDrawer, final ChartData data, final ChartPanel chartPanel,
			final LyricLinesDrawer lyricLinesDrawer, final SelectionManager selectionManager,
			final WaveFormDrawer waveFormDrawer) {
		this.beatsDrawer = beatsDrawer;
		this.data = data;
		this.chartPanel = chartPanel;
		this.lyricLinesDrawer = lyricLinesDrawer;
		this.selectionManager = selectionManager;
		this.waveFormDrawer = waveFormDrawer;
	}

	private void drawVocals(final Graphics g, final int time, final HighlightData highlightData) {
		final VocalNotesDrawingData drawingData = new VocalNotesDrawingData(g, time);

		final ArrayList2<Vocal> vocals = data.songChart.vocals.vocals;
		final int width = chartPanel.getWidth();
		final HashSet2<Integer> selectedVocalIds = selectionManager.getSelectedAccessor(PositionType.VOCAL)//
				.getSelectedSet().map(selection -> selection.id);

		final int timeFrom = xToTime(-1, time);
		final int timeTo = xToTime(width + 1, time);

		for (int i = 0; i < vocals.size(); i++) {
			final Vocal vocal = vocals.get(i);
			if (vocal.position() > timeTo) {
				break;
			}

			Vocal next = null;
			if (vocal.isWordPart()) {
				next = vocals.size() > i + 1 ? vocals.get(i + 1) : null;
			}
			final int endPosition = next == null ? vocal.endPosition() : next.position();
			if (endPosition < timeFrom) {
				continue;
			}

			final int x = timeToX(vocal.position(), time);
			final int length = max(2, timeToXLength(vocal.position(), vocal.length()));
			final boolean selected = selectedVocalIds.contains(i);
			drawingData.addVocal(vocal, next, x, length, selected);
		}

		if (highlightData.type == PositionType.VOCAL) {
			if (highlightData.id.isPresent()) {
				final Vocal vocal = vocals.get(highlightData.id.get().id);
				drawingData.addHighlight(time, vocal.position(), vocal.length());
			} else {
				highlightData.highlightedNonIdPositions.forEach(highlightPosition -> drawingData.addHighlight(time,
						highlightPosition.position, highlightPosition.length));
			}
		}

		drawingData.draw(g);
	}

	public void draw(final Graphics g, final int time, final HighlightData highlightData) {
		if (data == null || data.isEmpty) {
			return;
		}

		waveFormDrawer.draw(g, time);
		beatsDrawer.draw(g, time, highlightData);
		drawVocals(g, time, highlightData);
		lyricLinesDrawer.draw(g, time);
	}
}
