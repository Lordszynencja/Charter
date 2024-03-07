package log.charter.gui.chartPanelDrawers.common;

import static log.charter.data.config.Config.showGrid;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatSizeTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.editAreaHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.managers.RepeatManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.song.Beat;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;
import log.charter.util.grid.GridPosition;

public class BeatsDrawer {
	private static final Font beatFont = new Font(Font.DIALOG, Font.BOLD, 9);
	private static final NumberFormat bpmFormat = new DecimalFormat("##0.00");

	private static class BeatsDrawingData {
		private final DrawableShapeList beats = new DrawableShapeList();
		private final DrawableShapeList sectionsAndPhrases = new DrawableShapeList();
		private final DrawableShapeList bookmarks = new DrawableShapeList();
		private final DrawableShapeList repeat = new DrawableShapeList();

		private void addBeatLine(final int x, final Beat beat) {
			final ColorLabel color = beat.firstInMeasure ? ColorLabel.MAIN_BEAT : ColorLabel.SECONDARY_BEAT;
			beats.add(lineVertical(x, beatTextY, lanesBottom, color));

			if (beat.anchor) {
				final Position2D leftCorner = new Position2D(x - 2, beatTextY);
				final Position2D rightCorner = new Position2D(x + 3, beatTextY);
				final Position2D bottomCorner = new Position2D(x, beatTextY + 4);
				beats.add(filledTriangle(leftCorner, rightCorner, bottomCorner, ColorLabel.BEAT_MARKER));
			}
		}

		private void addBeatBarNumber(final int x, final int barNumber) {
			final String text = "" + barNumber;
			beats.add(new Text(new Position2D(x + 3, beatTextY + 1), beatFont, text, ColorLabel.ARRANGEMENT_TEXT));
		}

		private void addBeatBarNumber(final int x, final int barNumber, final String bpmValue) {
			final String text = "" + barNumber + " (" + bpmValue + " BPM)";
			beats.add(new Text(new Position2D(x + 3, beatTextY + 1), beatFont, text, ColorLabel.ARRANGEMENT_TEXT));
		}

		private void addBPMNumber(final int x, final String bpmValue) {
			final String text = "(" + bpmValue + " BPM)";
			beats.add(new Text(new Position2D(x + 3, beatTextY + 1), beatFont, text, ColorLabel.ARRANGEMENT_TEXT));
		}

		private void addTimeSignatureChange(final int x, final Beat beat) {
			beats.add(new Text(new Position2D(x + 3, beatSizeTextY + 1), beatFont,
					beat.beatsInMeasure + "/" + beat.noteDenominator, ColorLabel.ARRANGEMENT_TEXT));
		}

		private void addBeatBox(final int x, final ColorLabel color) {
			final int top = beatTextY - 1;
			final int bottom = lanesBottom + 1;
			final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
			beats.add(strokedRectangle(beatPosition, color));
		}

		public void addBeat(final Beat beat, final int x, final int barNumber, final Beat previousBeat,
				final double bpm, final boolean selected, final boolean highlighted) {
			addBeatLine(x, beat);

			if (beat.firstInMeasure) {
				if (previousBeat == null || beat.anchor) {
					addBeatBarNumber(x, barNumber, bpmFormat.format(bpm));
				} else {
					addBeatBarNumber(x, barNumber);
				}
			} else if (beat.anchor) {
				addBPMNumber(x, bpmFormat.format(bpm));
			}

			if (previousBeat == null || beat.beatsInMeasure != previousBeat.beatsInMeasure
					|| beat.noteDenominator != previousBeat.noteDenominator) {
				addTimeSignatureChange(x, beat);
			}

			if (highlighted) {
				addBeatBox(x, ColorLabel.SELECT);
			} else if (selected) {
				addBeatBox(x, ColorLabel.SELECT);
			}
		}

		public void addBeatHighlight(final int x) {
			final int top = beatTextY - 1;
			final int bottom = lanesBottom + 1;
			final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
			beats.add(strokedRectangle(beatPosition, ColorLabel.HIGHLIGHT));
		}

		public void addGrid(final int x) {
			beats.add(lineVertical(x, beatTextY + beatSizeTextY, lanesBottom, ColorLabel.GRID));
		}

		public void addBookmark(final int number, final int x) {
			bookmarks.add(lineVertical(x, 0, editAreaHeight, ColorLabel.BOOKMARK));
			bookmarks.add(new Text(new Position2D(x + 2, 1), beatFont, number + "", ColorLabel.BOOKMARK));
		}

		public void addRepeatStart(final int x) {
			repeat.add(lineVertical(x, 0, beatTextY, ColorLabel.REPEAT_MARKER));
			final ShapePositionWithSize startPosition = new ShapePositionWithSize(x, 0, 10, 3);
			repeat.add(filledRectangle(startPosition, ColorLabel.REPEAT_MARKER));
		}

		public void addRepeatEnd(final int x) {
			repeat.add(lineVertical(x, 0, beatTextY, ColorLabel.REPEAT_MARKER));
			final ShapePositionWithSize endPosition = new ShapePositionWithSize(x - 10, 0, 10, 3);
			repeat.add(filledRectangle(endPosition, ColorLabel.REPEAT_MARKER));
		}

		public void addFullRepeat(final int x0, final int x1) {
			repeat.add(lineVertical(x0, 0, beatTextY, ColorLabel.REPEAT_MARKER));
			repeat.add(lineVertical(x1, 0, beatTextY, ColorLabel.REPEAT_MARKER));
			final ShapePositionWithSize fullPosition = new ShapePositionWithSize(x0, 0, x1 - x0, 3);
			repeat.add(filledRectangle(fullPosition, ColorLabel.REPEAT_MARKER));
		}

		public void draw(final Graphics2D g) {
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
			beats.draw(g);
			repeat.draw(g);
			sectionsAndPhrases.draw(g);
			bookmarks.draw(g);
		}
	}

	private ChartData data;
	private ChartPanel chartPanel;
	private RepeatManager repeatManager;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final ChartPanel chartPanel, final RepeatManager repeatManager,
			final SelectionManager selectionManager) {
		this.data = data;
		this.chartPanel = chartPanel;
		this.repeatManager = repeatManager;
		this.selectionManager = selectionManager;
	}

	private void addBeats(final int time, final BeatsDrawingData drawingData, final HighlightData highlightData) {
		final List<Beat> beats = data.songChart.beatsMap.beats;
		final HashSet2<Integer> selectedBeatIds = selectionManager.getSelectedAccessor(PositionType.BEAT)//
				.getSelectedSet().map(selection -> selection.id);
		final int highlightId = highlightData.getId(PositionType.BEAT);

		double bpm = 120;
		int bar = 0;
		for (int i = 0; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			if (beat.firstInMeasure) {
				bar++;
			}
			if (i == 0 || (beat.anchor && i < beats.size() - 1)) {
				bpm = data.songChart.beatsMap.findBPM(beat, i);
			}

			final int x = timeToX(beat.position(), time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final boolean selected = selectedBeatIds.contains(i);
			final boolean highlighted = i == highlightId;
			drawingData.addBeat(beat, x, bar, i > 0 ? beats.get(i - 1) : null, bpm, selected, highlighted);
		}

		if (highlightData.type == PositionType.BEAT) {
			highlightData.highlightedNonIdPositions.forEach(
					highlightPosition -> drawingData.addBeatHighlight(timeToX(highlightPosition.position, time)));
		}
	}

	private void addRepeater(final int time, final BeatsDrawingData drawingData) {
		final int start = repeatManager.getRepeatStart();
		final int end = repeatManager.getRepeatEnd();
		if (start >= 0 && end >= 0) {
			if (start > end) {
				drawingData.addRepeatStart(timeToX(start, time));
				drawingData.addRepeatEnd(timeToX(end, time));
			} else {
				drawingData.addFullRepeat(timeToX(start, time), timeToX(end, time));
			}
		} else if (start >= 0) {
			drawingData.addRepeatStart(timeToX(start, time));
		} else if (end >= 0) {
			drawingData.addRepeatEnd(timeToX(end, time));
		}
	}

	private void addGrid(final int time, final BeatsDrawingData drawingData) {
		final GridPosition<Beat> gridPosition = GridPosition.create(data.songChart.beatsMap.beats, xToTime(0, time));
		final int maxTime = xToTime(chartPanel.getWidth() + 1, time);
		while (gridPosition.position() < maxTime) {
			if (gridPosition.positionId >= data.songChart.beatsMap.beats.size() - 1) {
				break;
			}
			if (gridPosition.gridId != 0) {
				drawingData.addGrid(timeToX(gridPosition.position(), time));
			}
			gridPosition.next();
		}
	}

	private void addBookmarks(final int time, final BeatsDrawingData drawingData) {
		data.songChart.bookmarks.forEach((number, position) -> {
			final int x = timeToX(position, time);
			drawingData.addBookmark(number, x);
		});
	}

	public void draw(final Graphics2D g, final int time, final HighlightData highlightData) {
		final BeatsDrawingData drawingData = new BeatsDrawingData();

		addBeats(time, drawingData, highlightData);
		addRepeater(time, drawingData);

		if (showGrid) {
			addGrid(time, drawingData);
		}

		addBookmarks(time, drawingData);

		drawingData.draw(g);
	}

}
