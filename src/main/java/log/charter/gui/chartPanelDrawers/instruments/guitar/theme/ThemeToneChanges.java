package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.awt.Graphics2D;

import log.charter.song.ToneChange;

public interface ThemeToneChanges {
	void addCurrentTone(Graphics2D g, String tone, int nextToneChangeX);

	void addCurrentTone(Graphics2D g, String tone);

	void addToneChange(final ToneChange toneChange, final int x, final boolean selected, final boolean highlighted);

	void addToneChangeHighlight(final int x);

}
