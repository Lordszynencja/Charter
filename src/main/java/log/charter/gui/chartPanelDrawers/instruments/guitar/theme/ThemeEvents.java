package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import log.charter.song.EventPoint;
import log.charter.song.Phrase;

public interface ThemeEvents {
	void addEventPoint(final EventPoint eventPoint, final Phrase phrase, final int x, final boolean selected,
			final boolean highlighted);

	void addEventPointHighlight(final int x);
}
