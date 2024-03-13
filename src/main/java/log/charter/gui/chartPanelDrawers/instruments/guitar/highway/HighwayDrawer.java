package log.charter.gui.chartPanelDrawers.instruments.guitar.highway;

import java.awt.Graphics2D;
import java.util.Optional;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.song.Anchor;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightLine;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.ModernHighwayDrawer;

public interface HighwayDrawer {
	public static void reloadGraphics() {
		ModernHighwayDrawer.reloadGraphics();
	}

	public static HighwayDrawer getHighwayDrawer(final Graphics2D g, final int strings, final int time) {
		return switch (GraphicalConfig.theme) {
			case BASIC -> new DefaultHighwayDrawer(g, strings, time);
			case SQUARE -> new SquareHighwayDrawer(g, strings, time);
			case MODERN -> new ModernHighwayDrawer(g, strings, time);
		};
	}

	void addCurrentSection(Graphics2D g, SectionType section);

	void addCurrentSection(Graphics2D g, SectionType section, int nextSectionX);

	void addCurrentPhrase(Graphics2D g, Phrase phrase, String phraseName, int nextEventPointX);

	void addCurrentPhrase(Graphics2D g, Phrase phrase, String phraseName);

	void addEventPoint(Graphics2D g, EventPoint eventPoint, Phrase phrase, int x, boolean selected,
			boolean highlighted);

	void addEventPointHighlight(int x);

	void addCurrentTone(Graphics2D g, String tone);

	void addCurrentTone(Graphics2D g, String tone, int nextToneChangeX);

	void addToneChange(ToneChange toneChange, int x, boolean selected, boolean highlighted);

	void addToneChangeHighlight(int x);

	void addCurrentAnchor(Graphics2D g, Anchor anchor);

	void addCurrentAnchor(Graphics2D g, Anchor anchor, int nextAnchorX);

	void addAnchor(Anchor anchor, int x, boolean selected, boolean highlighted);

	void addAnchorHighlight(int x);

	void addChordName(int x, String chordName);

	void addNote(final EditorNoteDrawingData note);

	void addSoundHighlight(int x, Optional<ChordOrNote> originalSound, Optional<ChordTemplate> template, int string,
			final boolean drawOriginalStrings);

	void addNoteAdditionLine(HighlightLine line);

	void addHandShape(int x, int length, boolean selected, boolean highlighted, HandShape handShape,
			ChordTemplate chordTemplate);

	void addHandShapeHighlight(int x, int length);

	void draw(Graphics2D g);

}
