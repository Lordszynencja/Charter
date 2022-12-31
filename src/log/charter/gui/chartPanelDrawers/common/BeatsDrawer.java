package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatSizeTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.eventNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.phraseNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.sectionNamesY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.text;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.textWithBackground;
import static log.charter.util.ScalingUtils.timeToX;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShapeList;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.song.Beat;
import log.charter.song.Event;
import log.charter.song.Phrase;
import log.charter.song.PhraseIteration;
import log.charter.song.Section;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;

public class BeatsDrawer {
	private static class BeatsDrawingData {
		private static final Color selectColor = ColorLabel.SELECT.color();
		private static final Color mainBeatColor = ColorLabel.MAIN_BEAT.color();
		private static final Color secondaryBeatColor = ColorLabel.SECONDARY_BEAT.color();

		private final DrawableShapeList beats = new DrawableShapeList();
		private final DrawableShapeList sectionsAndPhrases = new DrawableShapeList();

		private void addBeatLine(final int x, final Beat beat) {
			final Color color = beat.firstInMeasure ? mainBeatColor : secondaryBeatColor;
			beats.add(lineVertical(x, beatTextY, lanesBottom, color));

			if (beat.anchor) {
				final Position2D leftCorner = new Position2D(x - 3, beatTextY);
				final Position2D rightCorner = new Position2D(x + 4, beatTextY);
				final Position2D bottomCorner = new Position2D(x, beatTextY + 3);
				beats.add(DrawableShape.filledTriangle(leftCorner, rightCorner, bottomCorner, color));
			}
		}

		private void addBeatMeasureNumber(final int x, final int id) {
			final String text = "" + (id + 1);
			beats.add(text(new Position2D(x + 3, beatTextY + 11), text, mainBeatColor));
		}

		private void addMeasureChange(final int x, final Beat beat) {
			beats.add(text(new Position2D(x + 3, beatSizeTextY + 11), beat.beatsInMeasure + "/4", mainBeatColor));
		}

		private void addSelect(final int x) {
			final int top = beatTextY - 1;
			final int bottom = lanesBottom + 1;
			final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 2, bottom - top);
			beats.add(strokedRectangle(beatPosition, selectColor));
		}

		public void addBeat(final Beat beat, final int x, final int id, final Beat previousBeat,
				final boolean selected) {
			addBeatLine(x, beat);

			if (beat.firstInMeasure) {
				addBeatMeasureNumber(x, id);
			}
			if (previousBeat == null || beat.beatsInMeasure != previousBeat.beatsInMeasure) {
				addMeasureChange(x, beat);
			}

			if (selected) {
				addSelect(x);
			}
		}

		public void addSection(final Section section, final int x) {
			final String sectionName = section.type.label;
			sectionsAndPhrases.add(textWithBackground(new Position2D(x, sectionNamesY + 11), sectionName,
					ColorLabel.SECTION_NAME_BG, ColorLabel.BASE_DARK_TEXT));
		}

		public void addPhrase(final Phrase phrase, final PhraseIteration phraseIteration, final int x) {
			final String phraseName = phraseIteration.phraseName + " (" + phrase.maxDifficulty + ")"//
					+ (phrase.solo ? "[Solo]" : "");
			sectionsAndPhrases.add(textWithBackground(new Position2D(x, phraseNamesY + 11), phraseName,
					ColorLabel.PHRASE_NAME_BG, ColorLabel.BASE_DARK_TEXT));
		}

		public void addEvents(final ArrayList2<Event> events, final int x) {
			final String eventsName = String.join(", " + events.map(event -> event.type.label));
			sectionsAndPhrases.add(textWithBackground(new Position2D(x, eventNamesY + 11), eventsName,
					ColorLabel.EVENT_BG, ColorLabel.BASE_DARK_TEXT));
		}

		public void draw(final Graphics g) {
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));
			beats.draw(g);
			sectionsAndPhrases.draw(g);
		}
	}

	private ChartData data;
	private ChartPanel chartPanel;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final ChartPanel chartPanel,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager) {
		this.data = data;
		this.chartPanel = chartPanel;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.selectionManager = selectionManager;
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

		for (int i = 0; i < beats.size(); i++) {
			final Beat beat = beats.get(i);
			final int x = timeToX(beat.position(), data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final boolean selected = selectedBeatIds.contains(i);
			drawingData.addBeat(beat, x, i, i > 0 ? beats.get(i - 1) : null, selected);
		}
	}

	private void addSections(final BeatsDrawingData drawingData) {
		for (final Section section : data.getCurrentArrangement().sections) {
			final int x = timeToX(section.beat.position(), data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			drawingData.addSection(section, x);
		}
	}

	private void addPhrases(final BeatsDrawingData drawingData) {
		for (final PhraseIteration phraseIteration : data.getCurrentArrangement().phraseIterations) {
			final int x = timeToX(phraseIteration.beat.position(), data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final Phrase phrase = data.getCurrentArrangement().phrases.get(phraseIteration.phraseName);
			drawingData.addPhrase(phrase, phraseIteration, x);
		}
	}

	private void addEvents(final BeatsDrawingData drawingData) {
		final HashMap2<Integer, ArrayList2<Event>> eventsOnPositions = new HashMap2<>();

		for (final Event event : data.getCurrentArrangement().events) {
			final int position = event.beat.position();
			ArrayList2<Event> beatEvents = eventsOnPositions.get(position);
			if (beatEvents == null) {
				beatEvents = new ArrayList2<>();
				eventsOnPositions.put(position, beatEvents);
			}

			beatEvents.add(event);
		}

		for (final Entry<Integer, ArrayList2<Event>> eventsOnPosition : eventsOnPositions.entrySet()) {
			final int x = timeToX(eventsOnPosition.getKey(), data.time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				continue;
			}

			drawingData.addEvents(eventsOnPosition.getValue(), x);
		}
	}

	public void draw(final Graphics g) {
		final BeatsDrawingData drawingData = new BeatsDrawingData();

		addBeats(drawingData);
		addSections(drawingData);
		addPhrases(drawingData);
		addEvents(drawingData);

		drawingData.draw(g);
	}

}
