package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.awt.Graphics2D;

import log.charter.data.song.FHP;

public interface ThemeFHPs {
	void addCurrentFHP(Graphics2D g, FHP fhp);

	void addCurrentFHP(Graphics2D g, FHP fhp, int nextFHPX);

	void addFHP(final FHP fhp, final int x, final boolean selected, final boolean highlighted);

	void addFHPHighlight(final int x);
}
