package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.awt.Graphics2D;

import log.charter.data.song.EventPoint;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;

public interface ThemeEvents {

	ShapeSize getSizeOfSection(Graphics2D g, SectionType section);

	ShapeSize getSizeOfPhrase(Graphics2D g, Phrase phrase, String phraseName);

	void addSection(Graphics2D g, SectionType section, int x, boolean highlight);

	void addPhrase(Graphics2D g, Phrase phrase, String phraseName, int x, boolean highlight);

	void addEvents(EventPoint eventPoint, int x);

	void addEventPoint(Graphics2D g, final EventPoint eventPoint, final Phrase phrase, final int x,
			final boolean selected, final boolean highlighted);

	void addEventPointHighlight(final int x);

}
