package log.charter.gui.chartPanelDrawers.instruments.guitar.theme.modern;

import java.awt.Graphics2D;
import java.util.Optional;

import log.charter.data.song.FHP;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Phrase;
import log.charter.data.song.SectionType;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightLine;
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
	public void addCurrentSection(final Graphics2D g, final SectionType section) {
		themeEvents.addCurrentSection(g, section);
	}

	@Override
	public void addCurrentSection(final Graphics2D g, final SectionType section, final int nextSectionX) {
		themeEvents.addCurrentSection(g, section, nextSectionX);
	}

	@Override
	public void addCurrentPhrase(final Graphics2D g, final Phrase phrase, final String phraseName) {
		themeEvents.addCurrentPhrase(g, phrase, phraseName);
	}

	@Override
	public void addCurrentPhrase(final Graphics2D g, final Phrase phrase, final String phraseName,
			final int nextSectionX) {
		themeEvents.addCurrentPhrase(g, phrase, phraseName, nextSectionX);
	}

	@Override
	public void addEventPoint(final Graphics2D g, final EventPoint eventPoint, final Phrase phrase, final int x,
			final boolean selected, final boolean highlighted) {
		themeEvents.addEventPoint(g, eventPoint, phrase, x, selected, highlighted);
	}

	@Override
	public void addEventPointHighlight(final int x) {
		themeEvents.addEventPointHighlight(x);
	}

	@Override
	public void addCurrentTone(final Graphics2D g, final String tone) {
		toneChanges.addCurrentTone(g, tone);
	}

	@Override
	public void addCurrentTone(final Graphics2D g, final String tone, final int nextToneChangeX) {
		toneChanges.addCurrentTone(g, tone, nextToneChangeX);
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
	public void addCurrentFHP(final Graphics2D g, final FHP fhp) {
		fhps.addCurrentFHP(g, fhp);
	}

	@Override
	public void addCurrentFHP(final Graphics2D g, final FHP fhp, final int nextFHPX) {
		fhps.addCurrentFHP(g, fhp, nextFHPX);
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