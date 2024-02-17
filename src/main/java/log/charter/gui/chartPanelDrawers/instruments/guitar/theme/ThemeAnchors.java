package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import log.charter.song.Anchor;

public interface ThemeAnchors {
	void addAnchor(final Anchor anchor, final int x, final boolean selected, final boolean highlighted);

	void addAnchorHighlight(final int x);
}
