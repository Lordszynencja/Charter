package log.charter.gui.chartPanelDrawers.instruments.guitar;

import java.awt.Graphics;

import log.charter.data.config.Config;
import log.charter.song.Anchor;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.ToneChange;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;

interface HighwayDrawer {
	public static HighwayDrawer getHighwayDrawer(final int strings, final int time) {
		return switch (Config.theme) {
		case DEFAULT -> new DefaultHighwayDrawer(strings, time);
		case ROCKSMITH -> new RocksmithHighwayDrawer(strings, time);
		};
	}

	void addToneChange(ToneChange toneChange, int x, boolean selected);

	void addAnchor(Anchor anchor, int x, boolean selected);

	void addChord(Chord chord, ChordTemplate chordTemplate, int x, int length, boolean selected,
			boolean lastWasLinkNext, boolean ctrl);

	void addNote(Note note, int x, int length, boolean selected, boolean lastWasLinkNext);

	void addHandShape(int x, int length, boolean selected, HandShape handShape, ChordTemplate chordTemplate);

	void draw(Graphics g);
}
