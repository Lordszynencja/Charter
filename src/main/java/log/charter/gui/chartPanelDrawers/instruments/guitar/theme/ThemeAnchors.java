package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.awt.Graphics2D;

import log.charter.data.song.Anchor;

public interface ThemeAnchors {
	void addCurrentAnchor(Graphics2D g, Anchor anchor);

	void addCurrentAnchor(Graphics2D g, Anchor anchor, int nextAnchorX);

	void addAnchor(final Anchor anchor, final int x, final boolean selected, final boolean highlighted);

	void addAnchorHighlight(final int x);
}
