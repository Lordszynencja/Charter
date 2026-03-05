package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.util.ScalingUtils.positionToX;

import java.util.List;
import java.util.Map;
import java.util.Set;

import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.types.PositionType;
import log.charter.data.types.SpecialPositionType;
import log.charter.gui.chartPanelDrawers.data.FrameData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.GuitarDrawerUtils.ItemWithDrawingPosition;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;

public class GuitarEventPointsDrawer {
	private static void drawPreviousSection(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final GuitarDrawerUtils guitarDrawerUtils, final boolean highlight) {
		final ItemWithDrawingPosition<EventPoint> section = guitarDrawerUtils.findPreviousSection(frameData.time,
				highwayDrawer);
		if (section != null) {
			highwayDrawer.addSection(section.item.section, section.x0, highlight);
		}
	}

	private static void drawNextSection(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final GuitarDrawerUtils guitarDrawerUtils, final boolean highlight) {
		final ItemWithDrawingPosition<EventPoint> section = guitarDrawerUtils.findNextSection(frameData.time,
				highwayDrawer);
		if (section != null) {
			highwayDrawer.addSection(section.item.section, section.x0, highlight);
		}
	}

	private static void drawPreviousPhrase(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final GuitarDrawerUtils guitarDrawerUtils, final boolean highlight) {
		final ItemWithDrawingPosition<EventPoint> phrase = guitarDrawerUtils.findPreviousPhrase(frameData.time,
				highwayDrawer);
		if (phrase != null) {
			final String phraseName = phrase.item.phrase;
			final Phrase phraseInfo = frameData.arrangement.phrases.get(phraseName);
			highwayDrawer.addPhrase(phraseInfo, phraseName, phrase.x0, highlight);
		}
	}

	private static void drawNextPhrase(final FrameData frameData, final HighwayDrawer highwayDrawer,
			final GuitarDrawerUtils guitarDrawerUtils, final boolean highlight) {
		final ItemWithDrawingPosition<EventPoint> phrase = guitarDrawerUtils.findNextPhrase(frameData.time,
				highwayDrawer);
		if (phrase != null) {
			final String phraseName = phrase.item.phrase;
			final Phrase phraseInfo = frameData.arrangement.phrases.get(phraseName);
			highwayDrawer.addPhrase(phraseInfo, phraseName, phrase.x0, highlight);
		}
	}

	private static void drawHighlightedPositions(final HighwayDrawer highwayDrawer, final double time,
			final HighlightData highlightData) {
		if (highlightData.type != PositionType.EVENT_POINT) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = positionToX(highlightPosition.position, time);
			highwayDrawer.addEventPointHighlight(x);
		}
	}

	public static void addEventPoints(final FrameData frameData, final int panelWidth,
			final HighwayDrawer highwayDrawer, final GuitarDrawerUtils guitarDrawerUtils) {
		final List<EventPoint> eventPoints = frameData.arrangement.eventPoints;
		final Map<String, Phrase> phrases = frameData.arrangement.phrases;
		final Set<Integer> selectedIds = frameData.selection.getSelectedIdsSet(PositionType.EVENT_POINT);
		final int highlightId = frameData.highlightData.getId(PositionType.EVENT_POINT);

		drawPreviousSection(frameData, highwayDrawer, guitarDrawerUtils,
				frameData.highlightData.specialType == SpecialPositionType.SECTION_PREVIOUS);
		drawPreviousPhrase(frameData, highwayDrawer, guitarDrawerUtils,
				frameData.highlightData.specialType == SpecialPositionType.PHRASE_PREVIOUS);

		for (int i = 0; i < frameData.arrangement.eventPoints.size(); i++) {
			final EventPoint eventPoint = eventPoints.get(i);
			final int x = positionToX(eventPoint.position(frameData.beats), frameData.time);
			if (x < 0) {
				highwayDrawer.addEvents(eventPoint, x);
				continue;
			}
			if (x > panelWidth) {
				break;
			}

			final boolean selected = selectedIds.contains(i);
			final boolean highlighted = i == highlightId;
			highwayDrawer.addEventPoint(eventPoint, phrases.get(eventPoint.phrase), x, selected, highlighted);
		}

		drawNextSection(frameData, highwayDrawer, guitarDrawerUtils,
				frameData.highlightData.specialType == SpecialPositionType.SECTION_NEXT);
		drawNextPhrase(frameData, highwayDrawer, guitarDrawerUtils,
				frameData.highlightData.specialType == SpecialPositionType.PHRASE_NEXT);

		drawHighlightedPositions(highwayDrawer, frameData.time, frameData.highlightData);
	}
}
