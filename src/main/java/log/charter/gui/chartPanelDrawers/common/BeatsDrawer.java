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
import static log.charter.util.ScalingUtils.positionToX;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import log.charter.data.config.Config;
import log.charter.data.song.Beat;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.Text;
import log.charter.util.data.Position2D;
import log.charter.util.grid.GridPosition;

public class BeatsDrawer {
	private static final Font beatFont = new Font(Font.DIALOG, Font.BOLD, 9);
	private static final NumberFormat bpmFormat = new DecimalFormat("##0.00");

	private static String formatBPM(final double bpm, final Beat beat) {
		if (Config.showTempoInsteadOfBPM) {
			return bpmFormat.format(bpm / beat.noteDenominator * 4) + " ♪";
		} else {
			return bpmFormat.format(bpm) + " BPM";
		}
	}

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

		private void addBeatBarNumber(final int x, final int barNumber, final String tempoString) {
			final String text = "" + barNumber + " (" + tempoString + ")";
			beats.add(new Text(new Position2D(x + 3, beatTextY + 1), beatFont, text, ColorLabel.ARRANGEMENT_TEXT));
		}

		private void addTempo(final int x, final String tempoString) {
			final String text = "(" + tempoString + ")";
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
					addBeatBarNumber(x, barNumber, formatBPM(bpm, beat));
				} else {
					addBeatBarNumber(x, barNumber);
				}
			} else if (beat.anchor) {
				addTempo(x, formatBPM(bpm, beat));
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

	private ChartPanel chartPanel;

	private void addBeats(final FrameData frameData, final BeatsDrawingData drawingData) {
		final List<Integer> selectedBeatIds = frameData.selection.getSelectedIds(PositionType.BEAT);
		final int highlightId = frameData.highlightData.getId(PositionType.BEAT);

		double bpm = 120;
		int bar = 0;
		for (int i = 0; i < frameData.beats.size(); i++) {
			final Beat beat = frameData.beats.get(i);
			if (beat.firstInMeasure) {
				bar++;
			}
			if (i == 0 || (beat.anchor && i < frameData.beats.size() - 1)) {
				bpm = frameData.beats.findBPM(beat, i);
			}

			final int x = positionToX(beat.position(), frameData.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final boolean selected = selectedBeatIds.contains(i);
			final boolean highlighted = i == highlightId;
			drawingData.addBeat(beat, x, bar, i > 0 ? frameData.beats.get(i - 1) : null, bpm, selected, highlighted);
		}

		if (frameData.highlightData.type == PositionType.BEAT) {
			frameData.highlightData.highlightedNonIdPositions.forEach(highlightPosition -> drawingData
					.addBeatHighlight(positionToX(highlightPosition.position, frameData.time)));
		}
	}

	private void addRepeater(final FrameData frameData, final BeatsDrawingData drawingData) {
		if (frameData.repeaterSpan.a != null && frameData.repeaterSpan.b != null) {
			final int start = frameData.repeaterSpan.a;
			final int end = frameData.repeaterSpan.b;
			if (start > end) {
				drawingData.addRepeatStart(positionToX(start, frameData.time));
				drawingData.addRepeatEnd(positionToX(end, frameData.time));
			} else {
				drawingData.addFullRepeat(positionToX(start, frameData.time), positionToX(end, frameData.time));
			}
		} else if (frameData.repeaterSpan.a != null) {
			drawingData.addRepeatStart(positionToX(frameData.repeaterSpan.a, frameData.time));
		} else if (frameData.repeaterSpan.b != null) {
			drawingData.addRepeatEnd(positionToX(frameData.repeaterSpan.b, frameData.time));
		}
	}

	private void addGrid(final FrameData frameData, final BeatsDrawingData drawingData) {
		final GridPosition<Beat> gridPosition = GridPosition.create(frameData.beats, xToPosition(0, frameData.time));
		final int maxTime = xToPosition(chartPanel.getWidth() + 1, frameData.time);
		while (gridPosition.position() < maxTime) {
			if (gridPosition.positionId >= frameData.beats.size() - 1) {
				break;
			}
			if (gridPosition.gridId != 0) {
				drawingData.addGrid(positionToX(gridPosition.position(), frameData.time));
			}

			gridPosition.next();
		}
	}

	private void addBookmarks(final FrameData frameData, final BeatsDrawingData drawingData) {
		frameData.bookmarks.forEach((number, position) -> {
			final int x = positionToX(position, frameData.time);
			drawingData.addBookmark(number, x);
		});
	}

	public void draw(final FrameData frameData) {
		final BeatsDrawingData drawingData = new BeatsDrawingData();

		addBeats(frameData, drawingData);
		addRepeater(frameData, drawingData);

		if (showGrid) {
			addGrid(frameData, drawingData);
		}

		addBookmarks(frameData, drawingData);

		drawingData.draw(frameData.g);
	}

}
