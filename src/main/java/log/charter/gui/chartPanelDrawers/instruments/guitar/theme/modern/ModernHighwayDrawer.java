package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import java.awt.Graphics;
import java.util.Optional;

import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightLine;
import log.charter.gui.chartPanelDrawers.instruments.guitar.HighwayDrawer;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawerData;
import log.charter.song.Anchor;
import log.charter.song.ChordTemplate;
import log.charter.song.EventPoint;
import log.charter.song.HandShape;
import log.charter.song.Phrase;
import log.charter.song.ToneChange;
import log.charter.song.notes.ChordOrNote;

public class ModernHighwayDrawer implements HighwayDrawer {
	public static void reloadGraphics() {
		ModernThemeEvents.reloadSizes();
		ModernThemeToneChanges.reloadSizes();
		ModernThemeAnchors.reloadGraphics();
		ModernThemeNotes.reloadGraphics();
		ModernThemeHandShapes.reloadGraphics();
	}

	static {
		reloadGraphics();
	}

	private final HighwayDrawerData data;

	private final ModernThemeEvents themeEvents;
	private final ModernThemeToneChanges toneChanges;
	private final ModernThemeAnchors anchors;
	private final ModernThemeNotes notes;
	private final ModernThemeHandShapes handShapes;

	public ModernHighwayDrawer(final Graphics g, final int strings, final int time) {
		data = new HighwayDrawerData(g, strings, time);

		themeEvents = new ModernThemeEvents(data);
		toneChanges = new ModernThemeToneChanges(data);
		anchors = new ModernThemeAnchors(data);
		notes = new ModernThemeNotes(data);
		handShapes = new ModernThemeHandShapes(data);
	}

	@Override
	public void addEventPoint(final EventPoint eventPoint, final Phrase phrase, final int x, final boolean selected,
			final boolean highlighted) {
		themeEvents.addEventPoint(eventPoint, phrase, x, selected, highlighted);
	}

	@Override
	public void addEventPointHighlight(final int x) {
		themeEvents.addEventPointHighlight(x);
	}

	@Override
	public void addToneChange(final ToneChange toneChange, final int x, final boolean selected,
			final boolean highlighted) {
		toneChanges.addToneChange(toneChange, x, selected, highlighted);
	}

	@Override
	public void addToneChangeHighlight(final int x) {
		toneChanges.addToneChangeHighlight(x);
	}

	@Override
	public void addAnchor(final Anchor anchor, final int x, final boolean selected, final boolean highlighted) {
		anchors.addAnchor(anchor, x, selected, highlighted);
	}

	@Override
	public void addAnchorHighlight(final int x) {
		anchors.addAnchorHighlight(x);
	}

	@Override
	public void addNote(final EditorNoteDrawingData note) {
		notes.addNote(note);
	}

	@Override
	public void addChordName(final int x, final String chordName) {
		notes.addChordName(x, chordName);
	}

	@Override
	public void addSoundHighlight(final int x, final Optional<ChordOrNote> originalSound,
			final Optional<ChordTemplate> template, final int string) {
		notes.addSoundHighlight(x, originalSound, template, string);
	}

	@Override
	public void addNoteAdditionLine(final HighlightLine line) {
		notes.addNoteAdditionLine(line.lineStart, line.lineEnd);
	}

	@Override
	public void addHandShape(final int x, final int length, final boolean selected, final boolean highlighted,
			final HandShape handShape, final ChordTemplate chordTemplate) {
		handShapes.addHandShape(x, length, selected, highlighted, handShape, chordTemplate);
	}

	@Override
	public void addHandShapeHighlight(final int x, final int length) {
		handShapes.addHandShapeHighlight(x, length);
	}

	@Override
	public void draw(final Graphics g) {
		data.draw();
	}
}