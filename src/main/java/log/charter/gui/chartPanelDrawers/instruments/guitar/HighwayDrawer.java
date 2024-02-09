package log.charter.gui.chartPanelDrawers.instruments.guitar;

import java.awt.Graphics;

import log.charter.data.config.GraphicalConfig;
import log.charter.song.Anchor;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;

interface HighwayDrawer {
	public static void reloadSizes() {
		ModernHighwayDrawer.reloadSizes();
	}

	public static HighwayDrawer getHighwayDrawer(final Graphics g, final int strings, final int time) {
		return switch (GraphicalConfig.theme) {
			case BASIC -> new DefaultHighwayDrawer(g, strings, time);
			case ROCKSMITH -> new RocksmithHighwayDrawer(g, strings, time);
			case MODERN -> new ModernHighwayDrawer(g, strings, time);
		};
	}

	void addToneChange(ToneChange toneChange, int x, boolean selected, boolean highlighted);

	void addToneChangeHighlight(int x);

	void addAnchor(Anchor anchor, int x, boolean selected, boolean highlighted);

	void addAnchorHighlight(int x);

	void addChord(Chord chord, ChordTemplate chordTemplate, int x, int length, boolean selected,
			boolean lastWasLinkNext, boolean wrongLinkNext, boolean ctrl);

	void addNote(Note note, int x, boolean selected, boolean lastWasLinkNext, boolean wrongLinkNext);

	void addHandShape(int x, int length, boolean selected, boolean highlighted, HandShape handShape,
			ChordTemplate chordTemplate);

	void addHandShapeHighlight(int x, int length);

	void draw(Graphics g);
}
