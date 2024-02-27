package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.GraphicalConfig.eventsChangeHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.eventNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.phraseNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.sectionNamesY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;

import java.awt.Font;

import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawerData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeEvents;
import log.charter.song.EventPoint;
import log.charter.song.EventType;
import log.charter.song.Phrase;
import log.charter.song.SectionType;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.Position2D;

public class ModernThemeEvents implements ThemeEvents {
	private static Font eventFont = new Font(Font.SANS_SERIF, Font.BOLD, eventsChangeHeight);

	public static void reloadSizes() {
		eventFont = new Font(Font.SANS_SERIF, Font.BOLD, eventsChangeHeight);
	}

	private final HighwayDrawerData data;

	public ModernThemeEvents(final HighwayDrawerData highwayDrawerData) {
		data = highwayDrawerData;
	}

	private void addSection(final SectionType section, final int x) {
		data.sectionsAndPhrases.add(new TextWithBackground(new Position2D(x, sectionNamesY), eventFont, section.label,
				ColorLabel.BASE_TEXT, ColorLabel.SECTION_NAME_BG, 2, ColorLabel.BASE_BORDER.color())); // changed
	}

	private void addPhrase(final Phrase phrase, final String phraseName, final int x) {
		final String phraseLabel = phraseName + " (" + phrase.maxDifficulty + ")"//
				+ (phrase.solo ? "[Solo]" : "");
		Font boldFont = eventFont.deriveFont(Font.BOLD);
		data.sectionsAndPhrases.add(new TextWithBackground(new Position2D(x, phraseNamesY), eventFont, phraseLabel,
				ColorLabel.BASE_TEXT, ColorLabel.PHRASE_NAME_BG, 2, ColorLabel.BASE_BORDER.color())); // changed
	}

	private void addEvents(final ArrayList2<EventType> events, final int x) {
		final String eventsName = String.join(", ", events.map(event -> event.label));
		data.sectionsAndPhrases.add(new TextWithBackground(new Position2D(x, eventNamesY), eventFont, eventsName,
				ColorLabel.BASE_TEXT, ColorLabel.EVENT_BG, 2, ColorLabel.BASE_BORDER.color())); // changed
	}

	private void addEventPointBox(final int x, final ColorLabel color) {
		final int top = sectionNamesY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 3, bottom - top);
		data.sectionsAndPhrases.add(filledRectangle(beatPosition, color));
	}

	public void addEventPoint(final EventPoint eventPoint, final Phrase phrase, final int x, final boolean selected,
			final boolean highlighted) {
		if (eventPoint.section != null) {
			addSection(eventPoint.section, x);
		}
		if (eventPoint.phrase != null) {
			addPhrase(phrase, eventPoint.phrase, x);
		}
		if (!eventPoint.events.isEmpty()) {
			addEvents(eventPoint.events, x);
		}

		if (highlighted) {
			addEventPointBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addEventPointBox(x, ColorLabel.SELECT);
		}
	}

	public void addEventPointHighlight(final int x) {
		data.sectionsAndPhrases.add(lineVertical(x, sectionNamesY, lanesBottom, ColorLabel.HIGHLIGHT));
	}
}
