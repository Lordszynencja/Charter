package log.charter.gui.chartPanelDrawers.instruments.guitar.highway;

import java.awt.Graphics2D;
import java.util.Optional;

import log.charter.data.config.GraphicalConfig;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightLine;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern.ModernHighwayDrawer;

public interface HighwayDrawer {
	public static void reloadGraphics() {
		ModernHighwayDrawer.reloadGraphics();
	}

	public static HighwayDrawer getHighwayDrawer(final Graphics2D g, final int strings, final double time) {
		return switch (GraphicalConfig.theme) {
			case BASIC -> new DefaultHighwayDrawer(g, strings, time);
			case SQUARE -> new SquareHighwayDrawer(g, strings, time);
			case MODERN -> new ModernHighwayDrawer(g, strings, time);
		};
	}

	default boolean supportsCurrentValues() {
		return false;
	}

	default ShapeSize getSizeOfSection(final SectionType section) {
		return new ShapeSize(0, 0);
	}

	default ShapeSize getSizeOfPhrase(final Phrase phrase, final String phraseName) {
		return new ShapeSize(0, 0);
	}

	default ShapeSize getSizeOfTone(final String tone) {
		return new ShapeSize(0, 0);
	}

	void addSection(final SectionType section, int x, boolean highlight);

	void addPhrase(final Phrase phrase, String phraseName, int x, boolean highlight);

	void addEvents(EventPoint eventPoint, int x);

	void addEventPoint(EventPoint eventPoint, Phrase phrase, int x, boolean selected, boolean highlighted);

	void addEventPointHighlight(int x);

	void addTone(String tone, int x, boolean highlighted);

	void addToneChange(ToneChange toneChange, int x, boolean selected, boolean highlighted);

	void addToneChangeHighlight(int x);

	default void addCurrentFHP(final FHP fhp) {
	}

	default void addCurrentFHP(final FHP fhp, final int nextFHPX) {
	}

	void addFHP(FHP fhp, int x, boolean selected, boolean highlighted);

	void addFHPHighlight(int x);

	void addChordName(int x, String chordName);

	void addNote(final EditorNoteDrawingData note);

	void addSoundHighlight(int x, int length, Optional<ChordOrNote> originalSound, Optional<ChordTemplate> template,
			int string, boolean drawOriginalStrings);

	void addNoteAdditionLine(HighlightLine line);

	void addHandShape(int x, int length, boolean selected, boolean highlighted, HandShape handShape,
			ChordTemplate chordTemplate);

	void addHandShapeHighlight(int x, int length);

	void draw(Graphics2D g);

}
