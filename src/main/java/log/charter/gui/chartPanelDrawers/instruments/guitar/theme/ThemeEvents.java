package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.awt.Graphics2D;

import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;

public interface ThemeEvents {

	void addCurrentSection(Graphics2D g, SectionType section);

	void addCurrentSection(Graphics2D g, SectionType section, int nextSectionX);

	void addCurrentPhrase(Graphics2D g, Phrase phrase, String phraseName);

	void addCurrentPhrase(Graphics2D g, Phrase phrase, String phraseName, int nextSectionX);

	void addEventPoint(Graphics2D g, final EventPoint eventPoint, final Phrase phrase, final int x,
			final boolean selected, final boolean highlighted);

	void addEventPointHighlight(final int x);

}
