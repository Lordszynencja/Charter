package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import log.charter.song.ToneChange;

public interface ThemeToneChanges {
	void addToneChange(final ToneChange toneChange, final int x, final boolean selected, final boolean highlighted);

	void addToneChangeHighlight(final int x);
}
