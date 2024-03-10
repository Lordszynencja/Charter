package log.charter.gui.chartPanelDrawers.instruments.guitar.theme;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;

public interface ThemeHandShapes {
	void addHandShape(final int x, final int length, final boolean selected, final boolean highlighted,
			final HandShape handShape, final ChordTemplate chordTemplate);

	void addHandShapeHighlight(final int x, final int length);
}
