package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static log.charter.data.config.GraphicalConfig.eventsChangeHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.eventNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.phraseNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.sectionNamesY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRoundRectangle;
import static log.charter.util.CollectionUtils.map;

import java.awt.Font;
import java.awt.Graphics2D;

import log.charter.data.config.ChartPanelColors.ColorLabel;
import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.drawableShapes.TextWithBackground;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.ThemeEvents;
import log.charter.util.data.Position2D;

public class ModernThemeEvents implements ThemeEvents {
	private static final int sectionTextSpace = 2;

	private static Font eventFont = new Font(Font.SANS_SERIF, Font.BOLD, eventsChangeHeight);

	public static void reloadSizes() {
		eventFont = new Font(Font.SANS_SERIF, Font.BOLD, eventsChangeHeight);
	}

	private final HighwayDrawData data;

	public ModernThemeEvents(final HighwayDrawData highwayDrawerData) {
		data = highwayDrawerData;
	}

	private TextWithBackground generateText(final String text, final int x, final int y, final ColorLabel bgColor) {
		return new TextWithBackground(new Position2D(x, y + 3), eventFont, text, ColorLabel.ARRANGEMENT_TEXT, bgColor,
				sectionTextSpace, ColorLabel.BASE_BORDER);
	}

	private void addEventPointBox(final int x, final ColorLabel color) {
		final int top = sectionNamesY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 3, bottom - top);
		data.sectionsAndPhrases.add(filledRectangle(beatPosition, color));
	}

	@Override
	public ShapeSize getSizeOfSection(final Graphics2D g, final SectionType section) {
		return TextWithBackground.getExpectedSize(g, eventFont, section.label.label(), sectionTextSpace);
	}

	@Override
	public ShapeSize getSizeOfPhrase(final Graphics2D g, final Phrase phrase, final String phraseName) {
		final String label = generatePhraseLabel(phrase, phraseName);
		return TextWithBackground.getExpectedSize(g, eventFont, label, sectionTextSpace);
	}

	@Override
	public void addSection(final Graphics2D g, final SectionType section, final int x, final boolean highlight) {
		final String label = section.label.label();
		data.sectionsAndPhrases.add(generateText(label, x, sectionNamesY, ColorLabel.SECTION_NAME_BG));
		if (highlight) {
			final ShapeSize expectedSize = TextWithBackground.getExpectedSize(g, eventFont, label, sectionTextSpace);
			final ShapePositionWithSize position = new ShapePositionWithSize(x + 1, sectionNamesY + 2,
					expectedSize.width - 3, expectedSize.height - 3);
			data.sectionsAndPhrases.add(strokedRoundRectangle(position, ColorLabel.HIGHLIGHT.color(), 3, 3));
		}
	}

	@Override
	public void addPhrase(final Graphics2D g, final Phrase phrase, final String phraseName, final int x,
			final boolean highlight) {
		final String label = generatePhraseLabel(phrase, phraseName);
		data.sectionsAndPhrases.add(generateText(label, x, phraseNamesY, ColorLabel.PHRASE_NAME_BG));
		if (highlight) {
			final ShapeSize expectedSize = TextWithBackground.getExpectedSize(g, eventFont, label, sectionTextSpace);
			final ShapePositionWithSize position = new ShapePositionWithSize(x + 1, phraseNamesY + 2,
					expectedSize.width - 3, expectedSize.height - 3);
			data.sectionsAndPhrases.add(strokedRoundRectangle(position, ColorLabel.HIGHLIGHT.color(), 3, 3));
		}
	}

	private String generatePhraseLabel(final Phrase phrase, final String phraseName) {
		final StringBuilder b = new StringBuilder(phraseName);

		if (phrase.maxDifficulty > 0) {
			b.append(" (").append(phrase.maxDifficulty).append(")");
		}
		if (phrase.solo) {
			b.append(" [Solo]");
		}
		return b.toString();
	}

	@Override
	public void addEvents(final EventPoint eventPoint, final int x) {
		if (!eventPoint.events.isEmpty()) {
			final String text = String.join(", ", map(eventPoint.events, event -> event.label));
			data.sectionsAndPhrases.add(generateText(text, x, eventNamesY, ColorLabel.EVENT_BG));
		}
	}

	@Override
	public void addEventPoint(final Graphics2D g, final EventPoint eventPoint, final Phrase phrase, final int x,
			final boolean selected, final boolean highlighted) {
		if (eventPoint.section != null) {
			addSection(g, eventPoint.section, x, highlighted);
		}
		if (eventPoint.phrase != null) {
			addPhrase(g, phrase, eventPoint.phrase, x, highlighted);
		}
		addEvents(eventPoint, x);

		if (highlighted) {
			addEventPointBox(x, ColorLabel.HIGHLIGHT);
		} else if (selected) {
			addEventPointBox(x, ColorLabel.SELECT);
		}
	}

	@Override
	public void addEventPointHighlight(final int x) {
		data.sectionsAndPhrases.add(lineVertical(x, sectionNamesY, lanesBottom, ColorLabel.HIGHLIGHT));
	}
}
