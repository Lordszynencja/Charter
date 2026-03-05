package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import java.awt.Graphics2D;

import log.charter.data.song.ToneChange;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;

public interface ThemeToneChanges {
	ShapeSize getSizeOfTone(Graphics2D g, String tone);

	void addTone(Graphics2D g, String tone, int x, boolean highlight);

	void addToneChange(Graphics2D g, ToneChange toneChange, int x, boolean selected, boolean highlighted);

	void addToneChangeHighlight(final int x);
}
