package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatSizeTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.editAreaHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.eventNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.phraseNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.sectionNamesY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledTriangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.textWithBackground;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.Beat;
import log.charter.song.EventPoint;
import log.charter.song.EventType;
import log.charter.song.Phrase;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;
import log.charter.util.grid.GridPosition;

public class BeatsDrawer {
	private static final NumberFormat bpmFormat = new DecimalFormat("##0.0");

	private static class BeatsDrawingData {
		private final DrawableShapeList beats = new DrawableShapeList();
		private final DrawableShapeList sectionsAndPhrases = new DrawableShapeList();
		private final DrawableShapeList bookmarks = new DrawableShapeList();

		private void addBeatLine(final int x, final Beat beat) {
			final ColorLabel color = beat.firstInMeasure ? ColorLabel.MAIN_BEAT : ColorLabel.SECONDARY_BEAT;
			beats.add(lineVertical(x, beatTextY, lanesBottom, color));

			if (beat.anchor) {
				final Position2D leftCorner = new Position2D(x - 4, beatTextY);
				final Position2D rightCorner = new Position2D(x + 5, beatTextY);
				final Position2D bottomCorner = new Position2D(x, beatTextY + 4);
				beats.add(filledTriangle(leftCorner, rightCorner, bottomCorner, color));
			}
		}

		private void addBeatBarNumber(final int x, final int barNumber, final String bpmValue) {
			final String text = "" + barNumber + " (" + bpmValue + " BPM)";
			beats.add(text(new Position2D(x + 3, beatTextY + 11), text, ColorLabel.MAIN_BEAT));
		}

		private void addBPMNumber(final int x, final String bpmValue) {
			final String text = "(" + bpmValue + " BPM)";
			beats.add(text(new Position2D(x + 3, beatTextY + 11), text, ColorLabel.SECONDARY_BEAT));
		}

		private void addMeasureChange(final int x, final Beat beat) {
			beats.add(text(new Position2D(x + 3, beatSizeTextY + 11), beat.beatsInMeasure + "/" + beat.noteDenominator,
					ColorLabel.MAIN_BEAT));
		}

		private void addSelect(final int x) {
			final int top = beatTextY - 1;
			final int bottom = lanesBottom + 1;
			final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
			beats.add(strokedRectangle(beatPosition, ColorLabel.SELECT));
		}

		public void addBeat(final Beat beat, final int x, final int barNumber, final Beat previousBeat,
				final double bpm, final boolean selected) {
			addBeatLine(x, beat);

			if (beat.firstInMeasure) {
				addBeatBarNumber(x, barNumber, bpmFormat.format(bpm));
			} else if (beat.anchor) {
				addBPMNumber(x, bpmFormat.format(bpm));
			}

			if (previousBeat == null || beat.beatsInMeasure != previousBeat.beatsInMeasure) {
				addMeasureChange(x, beat);
			}
			if (selected) {
				addSelect(x);
			}
		}

		public void addGrid(final int x) {
			beats.add(lineVertical(x, lanesTop, lanesBottom, ColorLabel.GRID));
		}

		private void addSection(final SectionType section, final int x) {
			sectionsAndPhrases.add(textWithBackground(new Position2D(x, sectionNamesY + 11), section.label,
					ColorLabel.SECTION_NAME_BG, ColorLabel.BASE_DARK_TEXT));
		}

		private void addPhrase(final Phrase phrase, final String phraseName, final int x) {
			final String phraseLabel = phraseName + " (" + phrase.maxDifficulty + ")"//
					+ (phrase.solo ? "[Solo]" : "");
			sectionsAndPhrases.add(textWithBackground(new Position2D(x, phraseNamesY + 11), phraseLabel,
					ColorLabel.PHRASE_NAME_BG, ColorLabel.BASE_DARK_TEXT));
		}

		private void addEvents(final ArrayList2<EventType> events, final int x) {
			final String eventsName = String.join(", ", events.map(event -> event.label));
			sectionsAndPhrases.add(textWithBackground(new Position2D(x, eventNamesY + 11), eventsName,
					ColorLabel.EVENT_BG, ColorLabel.BASE_DARK_TEXT));
		}

		public void addEventPoint(final EventPoint eventPoint, final Phrase phrase, final int x,
				final boolean selected) {
			if (eventPoint.section != null) {
				addSection(eventPoint.section, x);
			}
			if (eventPoint.phrase != null) {
				addPhrase(phrase, eventPoint.phrase, x);
			}
			if (!eventPoint.events.isEmpty()) {
				addEvents(eventPoint.events, x);
			}

			if (selected) {
				final int top = sectionNamesY - 1;
				final int bottom = lanesBottom + 1;
				final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 3, bottom - top);
				beats.add(filledRectangle(beatPosition, ColorLabel.SELECT));
			}
		}

		public void addBookmark(final int number, final int x) {
			bookmarks.add(lineVertical(x, 0, editAreaHeight, ColorLabel.BOOKMARK));
			bookmarks.add(text(new Position2D(x + 2, 12), number + "", ColorLabel.BOOKMARK));
		}

		public void draw(final Graphics g) {
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
			beats.draw(g);
			sectionsAndPhrases.draw(g);
			bookmarks.draw(g);
		}
	}

	private ChartData data;
	private ChartPanel chartPanel;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final ChartPanel chartPanel, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager) {
		this.data = data;
		this.chartPanel = chartPanel;
		this.modeManager = modeManager;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.selectionManager = selectionManager;
	}

	private double findBPM(final Beat beat, final int beatId) {
		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

		int nextAnchorId = beats.size() - 1;
		for (int i = beatId + 1; i < beats.size(); i++) {
			if (beats.get(i).anchor) {
				nextAnchorId = i;
				break;
			}
		}

		final Beat nextAnchor = beats.get(nextAnchorId);

		return 60_000.0 / (nextAnchor.position() - beat.position()) * (nextAnchorId - beatId);
	}

	private void addBeats(final BeatsDrawingData drawingData) {
		final List<Beat> beats = data.songChart.beatsMap.beats;
		final HashSet2<Integer> selectedBeatIds = selectionManager.getSelectedAccessor(PositionType.BEAT)//
				.getSelectedSet().map(selection -> selection.id);

		if (selectedBeatIds.isEmpty()) {
			final MouseButtonPressData pressData = mouseButtonPressReleaseHandler
					.getPressPosition(MouseButton.LEFT_BUTTON);
			if (pressData != null && pressData.highlight.beat != null) {
				selectedBeatIds.add(pressData.highlight.id);
			}
		}

		double bpm = 120;
		int bar = 0;
		for (int i = 0; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			if (beat.firstInMeasure) {
				bar++;
			}
			if (i == 0 || (beat.anchor && i < beats.size() - 1)) {
				bpm = findBPM(beat, i);
			}

			final int x = timeToX(beat.position(), data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final boolean selected = selectedBeatIds.contains(i);

			drawingData.addBeat(beat, x, bar, i > 0 ? beats.get(i - 1) : null, bpm, selected);
		}
	}

	private void addGrid(final BeatsDrawingData drawingData) {
		final GridPosition<Beat> gridPosition = GridPosition.create(data.songChart.beatsMap.beats,
				xToTime(0, data.time));
		final int maxTime = xToTime(chartPanel.getWidth() + 1, data.time);
		while (gridPosition.position() < maxTime) {
			if (gridPosition.positionId >= data.songChart.beatsMap.beats.size() - 1) {
				break;
			}
			if (gridPosition.gridId != 0) {
				drawingData.addGrid(timeToX(gridPosition.position(), data.time));
			}
			gridPosition.next();
		}
	}

	private void addEventPoints(final BeatsDrawingData drawingData) {
		final HashSet2<Integer> selectedEventPointIds = selectionManager.getSelectedAccessor(PositionType.EVENT_POINT)//
				.getSelectedSet().map(selection -> selection.id);

		if (selectedEventPointIds.isEmpty()) {
			final MouseButtonPressData pressData = mouseButtonPressReleaseHandler
					.getPressPosition(MouseButton.LEFT_BUTTON);
			if (pressData != null && pressData.highlight.eventPoint != null) {
				selectedEventPointIds.add(pressData.highlight.id);
			}
		}

		final ArrayList2<EventPoint> eventPoints = data.getCurrentArrangement().eventPoints;

		for (int i = 0; i < eventPoints.size(); i++) {
			final EventPoint eventPoint = eventPoints.get(i);
			final int x = timeToX(eventPoint.position(), data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final boolean selected = selectedEventPointIds.contains(i);
			drawingData.addEventPoint(eventPoint, data.getCurrentArrangement().phrases.get(eventPoint.phrase), x,
					selected);
		}
	}

	private void addBookmarks(final BeatsDrawingData drawingData) {
		data.songChart.bookmarks.forEach((number, position) -> {
			final int x = timeToX(position, data.time);
			drawingData.addBookmark(number, x);
		});
	}

	public void draw(final Graphics g) {
		final BeatsDrawingData drawingData = new BeatsDrawingData();

		addBeats(drawingData);

		if (Config.showGrid && modeManager.editMode != EditMode.TEMPO_MAP) {
			addGrid(drawingData);
		}

		if (modeManager.editMode == EditMode.GUITAR) {
			addEventPoints(drawingData);
		}

		addBookmarks(drawingData);

		drawingData.draw(g);
	}

}
