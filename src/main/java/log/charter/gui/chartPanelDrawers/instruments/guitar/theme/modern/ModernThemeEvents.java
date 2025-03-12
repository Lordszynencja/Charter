package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import static java.lang.Math.min;
import static log.charter.data.config.GraphicalConfig.eventsChangeHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.eventNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.phraseNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.sectionNamesY;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.filledRectangle;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
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

	private TextWithBackground generateSectionText(final SectionType section, final int x) {
		return generateText(section.label.label(), x, sectionNamesY, ColorLabel.SECTION_NAME_BG);
	}

	private TextWithBackground generatePhraseText(final String text, final int x) {
		return generateText(text, x, phraseNamesY, ColorLabel.PHRASE_NAME_BG);
	}

	private void addEventPointBox(final int x, final ColorLabel color) {
		final int top = sectionNamesY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(x - 1, top, 3, bottom - top);
		data.sectionsAndPhrases.add(filledRectangle(beatPosition, color));
	}

	@Override
	public void addCurrentSection(final Graphics2D g, final SectionType section) {
		data.sectionsAndPhrases.add(generateSectionText(section, 0));
	}

	@Override
	public void addCurrentSection(final Graphics2D g, final SectionType section, final int nextSectionX) {
		final ShapeSize expectedSize = TextWithBackground.getExpectedSize(g, eventFont, section.label.label(),
				sectionTextSpace);
		final int x = min(0, nextSectionX - expectedSize.width);

		data.sectionsAndPhrases.add(generateSectionText(section, x));
	}

	private String generatePhraseLabel(final Phrase phrase, final String phraseName) {
		return phraseName + " (" + phrase.maxDifficulty + ")"//
				+ (phrase.solo ? "[Solo]" : "");
	}

	@Override
	public void addCurrentPhrase(final Graphics2D g, final Phrase phrase, final String phraseName) {
		data.sectionsAndPhrases.add(generatePhraseText(generatePhraseLabel(phrase, phraseName), 0));
	}

	@Override
	public void addCurrentPhrase(final Graphics2D g, final Phrase phrase, final String phraseName,
			final int nextPhraseX) {
		if (nextPhraseX <= 0) {
			return;
		}

		final String label = generatePhraseLabel(phrase, phraseName);
		final ShapeSize expectedSize = TextWithBackground.getExpectedSize(g, eventFont, label, sectionTextSpace);
		final int x = min(0, nextPhraseX - expectedSize.width);

		data.sectionsAndPhrases.add(generatePhraseText(label, x));
	}

	@Override
	public void addEvents(final Graphics2D g, final EventPoint eventPoint, final int x) {
		if (!eventPoint.events.isEmpty()) {
			final String text = String.join(", ", map(eventPoint.events, event -> event.label));
			data.sectionsAndPhrases.add(generateText(text, x, eventNamesY, ColorLabel.EVENT_BG));
		}
	}

	@Override
	public void addEventPoint(final Graphics2D g, final EventPoint eventPoint, final Phrase phrase, final int x,
			final boolean selected, final boolean highlighted) {
		if (eventPoint.section != null) {
			final String text = eventPoint.section.label.label();
			data.sectionsAndPhrases.add(generateText(text, x, sectionNamesY, ColorLabel.SECTION_NAME_BG));
		}
		if (eventPoint.phrase != null) {
			final String text = generatePhraseLabel(phrase, eventPoint.phrase);
			data.sectionsAndPhrases.add(generateText(text, x, phraseNamesY, ColorLabel.PHRASE_NAME_BG));
		}
		addEvents(g, eventPoint, x);

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
