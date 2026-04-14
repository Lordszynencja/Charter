package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import java.awt.Graphics2D;
import java.util.Optional;

import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightLine;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapeSize;
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;
import log.charter.gui.chartPanelDrawers.instruments.guitar.theme.HighwayDrawData;

public class ModernHighwayDrawer implements HighwayDrawer {
	public static void reloadGraphics() {
		ModernThemeEvents.reloadSizes();
		ModernThemeToneChanges.reloadSizes();
		ModernThemeFHPs.reloadGraphics();
		ModernThemeNotes.reloadGraphics();
		ModernThemeHandShapes.reloadGraphics();
	}

	static {
		reloadGraphics();
	}

	private final HighwayDrawData data;

	private final ModernThemeEvents themeEvents;
	private final ModernThemeToneChanges toneChanges;
	private final ModernThemeFHPs fhps;
	private final ModernThemeNotes notes;
	private final ModernThemeHandShapes handShapes;

	public ModernHighwayDrawer(final Graphics2D g, final int strings, final double time) {
		data = new HighwayDrawData(g, strings, time);

		themeEvents = new ModernThemeEvents(data);
		toneChanges = new ModernThemeToneChanges(data);
		fhps = new ModernThemeFHPs(data);
		notes = new ModernThemeNotes(data);
		handShapes = new ModernThemeHandShapes(data);
	}

	@Override
	public boolean supportsCurrentValues() {
		return true;
	}

	@Override
	public ShapeSize getSizeOfSection(final SectionType section) {
		return themeEvents.getSizeOfSection(data.g, section);
	}

	@Override
	public ShapeSize getSizeOfPhrase(final Phrase phrase, final String phraseName) {
		return themeEvents.getSizeOfPhrase(data.g, phrase, phraseName);
	}

	@Override
	public ShapeSize getSizeOfTone(final String tone) {
		return toneChanges.getSizeOfTone(data.g, tone);
	}

	@Override
	public void addSection(final SectionType section, final int x, final boolean highlight) {
		themeEvents.addSection(data.g, section, x, highlight);
	}

	@Override
	public void addPhrase(final Phrase phrase, final String phraseName, final int x, final boolean highlight) {
		themeEvents.addPhrase(data.g, phrase, phraseName, x, highlight);
	}

	@Override
	public void addEvents(final EventPoint eventPoint, final int x) {
		themeEvents.addEvents(eventPoint, x);
	}

	@Override
	public void addEventPoint(final EventPoint eventPoint, final Phrase phrase, final int x, final boolean selected,
			final boolean highlighted) {
		themeEvents.addEventPoint(data.g, eventPoint, phrase, x, selected, highlighted);
	}

	@Override
	public void addEventPointHighlight(final int x) {
		themeEvents.addEventPointHighlight(x);
	}

	@Override
	public void addTone(final String tone, final int x, final boolean highlighted) {
		toneChanges.addTone(data.g, tone, x, highlighted);
	}

	@Override
	public void addToneChange(final ToneChange toneChange, final int x, final boolean selected,
			final boolean highlighted) {
		toneChanges.addToneChange(data.g, toneChange, x, selected, highlighted);
	}

	@Override
	public void addToneChangeHighlight(final int x) {
		toneChanges.addToneChangeHighlight(x);
	}

	@Override
	public void addCurrentFHP(final FHP fhp) {
		fhps.addCurrentFHP(data.g, fhp);
	}

	@Override
	public void addCurrentFHP(final FHP fhp, final int nextFHPX) {
		fhps.addCurrentFHP(data.g, fhp, nextFHPX);
	}

	@Override
	public void addFHP(final FHP fhp, final int x, final boolean selected, final boolean highlighted) {
		fhps.addFHP(fhp, x, selected, highlighted);
	}

	@Override
	public void addFHPHighlight(final int x) {
		fhps.addFHPHighlight(x);
	}

	@Override
	public void addChordBox(final int x, final Chord chord) {
		notes.addChordBox(x, chord);
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
	public void addSoundHighlight(final int x, final int length, final Optional<ChordOrNote> originalSound,
			final Optional<ChordTemplate> template, final int string, final boolean drawOriginalStrings) {
		notes.addSoundHighlight(x, length, originalSound, template, string, drawOriginalStrings);
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
	public void draw(final Graphics2D g) {
		data.draw();
	}
}