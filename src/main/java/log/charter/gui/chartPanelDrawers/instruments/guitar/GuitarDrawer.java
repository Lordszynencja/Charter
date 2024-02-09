package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.max;
import static log.charter.data.config.GraphicalConfig.noteWidth;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.HighwayDrawer.getHighwayDrawer;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Graphics;
import java.math.BigDecimal;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.song.Anchor;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.ToneChange;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class GuitarDrawer {
	public static void reloadSizes() {
		HighwayDrawer.reloadSizes();
	}

	public static final BigDecimal bendStepSize = new BigDecimal("10");

	private static boolean isPastRightEdge(final int x, final int width) {
		return x > (width + noteWidth / 2);
	}

	private static boolean isOnScreen(final int x, final int length) {
		return x + length >= 0;
	}

	private WaveFormDrawer audioDrawer;
	private BeatsDrawer beatsDrawer;
	private ChartData data;
	private ChartPanel chartPanel;
	private KeyboardHandler keyboardHandler;
	private LyricLinesDrawer lyricLinesDrawer;
	private SelectionManager selectionManager;

	public void init(final WaveFormDrawer audioDrawer, final BeatsDrawer beatsDrawer, final ChartData data,
			final ChartPanel chartPanel, final KeyboardHandler keyboardHandler, final LyricLinesDrawer lyricLinesDrawer,
			final SelectionManager selectionManager) {
		this.audioDrawer = audioDrawer;
		this.beatsDrawer = beatsDrawer;
		this.data = data;
		this.chartPanel = chartPanel;
		this.keyboardHandler = keyboardHandler;
		this.lyricLinesDrawer = lyricLinesDrawer;
		this.selectionManager = selectionManager;
	}

	private void drawGuitarLanes(final Graphics g) {
		final int lanes = data.getCurrentArrangement().tuning.strings;
		final int width = chartPanel.getWidth();

		final int x = max(0, timeToX(0, data.time));

		for (int i = 0; i < lanes; i++) {
			g.setColor(getStringBasedColor(StringColorLabelType.LANE, i, lanes));
			final int y = getLaneY(getStringPosition(i, lanes));
			g.drawLine(x, y, width, y);
		}
	}

	private HashSet2<Integer> getSelectedIds(final PositionType positionType) {
		return selectionManager.getSelectedAccessor(positionType)//
				.getSelectedSet().map(selection -> selection.id);
	}

	private void addToneChanges(final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final ArrangementChart arrangement, final int panelWidth) {
		final HashSet2<Integer> selectedToneChangeIds = getSelectedIds(PositionType.TONE_CHANGE);
		final ArrayList2<ToneChange> toneChanges = arrangement.toneChanges;
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.TONE_CHANGE
				&& highlightData.highlightedId != null;

		for (int i = 0; i < toneChanges.size(); i++) {
			final ToneChange toneChange = toneChanges.get(i);
			final int x = timeToX(toneChange.position(), data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			if (!isOnScreen(x, 100)) {
				continue;
			}

			final boolean selected = selectedToneChangeIds.contains(i);
			final boolean highlighted = canBeHighlighted && i == highlightData.highlightedId;
			highwayDrawer.addToneChange(toneChange, x, selected, highlighted);
		}

		if (highlightData.highlightType == PositionType.TONE_CHANGE) {
			highlightData.highlightedNonIdPositions.forEach(highlightPosition -> highwayDrawer
					.addToneChangeHighlight(timeToX(highlightPosition.position, data.time)));
		}
	}

	private void addAnchors(final HighwayDrawer highwayDrawer, final HighlightData highlightData, final Level level,
			final int panelWidth) {
		final HashSet2<Integer> selectedAnchorIds = getSelectedIds(PositionType.ANCHOR);
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.ANCHOR
				&& highlightData.highlightedId != null;

		for (int i = 0; i < level.anchors.size(); i++) {
			final Anchor anchor = level.anchors.get(i);
			final int x = timeToX(anchor.position(), data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			if (!isOnScreen(x, 20)) {
				continue;
			}

			final boolean selected = selectedAnchorIds.contains(i);
			final boolean highlighted = canBeHighlighted && i == highlightData.highlightedId;
			highwayDrawer.addAnchor(anchor, x, selected, highlighted);
		}

		if (highlightData.highlightType == PositionType.ANCHOR) {
			highlightData.highlightedNonIdPositions.forEach(highlightPosition -> highwayDrawer
					.addAnchorHighlight(timeToX(highlightPosition.position, data.time)));
		}
	}

	private boolean addChord(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement,
			final int panelWidth, final Chord chord, final boolean selected, final boolean lastWasLinkNext,
			final boolean wrongLinkNext) {
		final int x = timeToX(chord.position(), data.time);
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		final int length = timeToXLength(chord.position(), chord.length());
		if (!isOnScreen(x, length)) {
			return true;
		}

		final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.templateId());
		highwayDrawer.addChord(chord, chordTemplate, x, length, selected, lastWasLinkNext, wrongLinkNext,
				keyboardHandler.ctrl());
		return true;
	}

	private boolean addNote(final HighwayDrawer highwayDrawer, final int panelWidth, final Note note,
			final boolean selected, final boolean lastWasLinkNext, final boolean wrongLinkNext) {
		final int x = timeToX(note.position(), data.time);
		final int length = timeToXLength(note.position(), note.length());
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		if (!isOnScreen(x, length)) {
			return true;
		}

		highwayDrawer.addNote(note, x, selected, lastWasLinkNext, wrongLinkNext);

		return true;
	}

	private boolean addChordOrNote(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement,
			final int panelWidth, final ChordOrNote chordOrNote, final boolean selected, final boolean lastWasLinkNext,
			final boolean wrongLinkNext) {
		if (chordOrNote.chord != null) {
			return addChord(highwayDrawer, arrangement, panelWidth, chordOrNote.chord, selected, lastWasLinkNext,
					wrongLinkNext);
		}
		if (chordOrNote.note != null) {
			return addNote(highwayDrawer, panelWidth, chordOrNote.note, selected, lastWasLinkNext, wrongLinkNext);
		}

		return true;
	}

	private boolean isLinkNextIncorrect(final int id, final int string, final int fret) {
		final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(string, id - 1,
				data.getCurrentArrangementLevel().chordsAndNotes);

		if (previousSound == null) {
			return true;
		}
		if (previousSound.isChord()) {
			final ChordNote chordNote = previousSound.chord.chordNotes.get(string);
			if (chordNote == null) {
				return true;
			}

			if (chordNote.slideTo == null) {
				final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates
						.get(previousSound.chord.templateId());
				if (chordTemplate.frets.getOrDefault(string, -1) != fret) {
					return true;
				}
			} else {
				if (chordNote.unpitchedSlide) {
					return true;
				}
				if (chordNote.slideTo != fret) {
					return true;
				}
			}
		} else {
			if (previousSound.note.string != string) {
				return true;
			}
			if (previousSound.note.slideTo == null) {
				if (previousSound.note.fret != fret) {
					return true;
				}
			} else {
				if (previousSound.note.unpitchedSlide) {
					return true;
				}
				if (previousSound.note.slideTo != fret) {
					return true;
				}
			}
		}

		return false;
	}

	private void addGuitarNotes(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement,
			final int panelWidth) {
		final HashSet2<Integer> selectedNoteIds = getSelectedIds(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordOrNote> chordsAndNotes = data.getCurrentArrangementLevel().chordsAndNotes;

		boolean lastWasLinkNext = false;
		for (int i = 0; i < chordsAndNotes.size(); i++) {
			final ChordOrNote sound = chordsAndNotes.get(i);
			final boolean wrongLinkNext;
			if (sound.isNote() && lastWasLinkNext) {
				wrongLinkNext = isLinkNextIncorrect(i, sound.note.string, sound.note.fret);
			} else {
				wrongLinkNext = false;
			}

			final boolean selected = selectedNoteIds.contains(i);
			addChordOrNote(highwayDrawer, arrangement, panelWidth, sound, selected, lastWasLinkNext, wrongLinkNext);

			lastWasLinkNext = sound.chord != null ? sound.chord.linkNext() : sound.note.linkNext;
		}
	}

	private void addHandShapes(final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final ArrangementChart arrangement, final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedHandShapeIds = getSelectedIds(PositionType.HAND_SHAPE);
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.HAND_SHAPE
				&& highlightData.highlightedId != null;

		for (int i = 0; i < level.handShapes.size(); i++) {
			final HandShape handShape = level.handShapes.get(i);
			final int x = timeToX(handShape.position(), data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final int length = timeToXLength(handShape.position(), handShape.length());
			if (!isOnScreen(x, length)) {
				continue;
			}

			final ChordTemplate chordTemplate;
			if (handShape.templateId >= 0 && arrangement.chordTemplates.size() > handShape.templateId) {
				chordTemplate = arrangement.chordTemplates.get(handShape.templateId);
			} else {
				chordTemplate = new ChordTemplate();
			}

			final boolean selected = selectedHandShapeIds.contains(i);
			final boolean highlighted = canBeHighlighted && i == highlightData.highlightedId;
			highwayDrawer.addHandShape(x, length, selected, highlighted, handShape, chordTemplate);
		}

		if (highlightData.highlightType == PositionType.HAND_SHAPE) {
			highlightData.highlightedNonIdPositions.forEach(highlightPosition -> highwayDrawer.addHandShapeHighlight(
					timeToX(highlightPosition.position, data.time),
					timeToXLength(highlightPosition.position, highlightPosition.length)));
		}
	}

	private void drawGuitarNotes(final Graphics g, final HighlightData highlightData) {
		final Level level = data.getCurrentArrangementLevel();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final int strings = data.getCurrentArrangement().tuning.strings;
		final HighwayDrawer highwayDrawer = getHighwayDrawer(g, strings, data.time);

		final int panelWidth = chartPanel.getWidth();

		addToneChanges(highwayDrawer, highlightData, arrangement, panelWidth);
		addAnchors(highwayDrawer, highlightData, level, panelWidth);
		addGuitarNotes(highwayDrawer, arrangement, panelWidth);
		addHandShapes(highwayDrawer, highlightData, arrangement, level, panelWidth);

		highwayDrawer.draw(g);
	}

	public void draw(final Graphics g, final HighlightData highlightData) {
		try {
			beatsDrawer.draw(g, highlightData);
			lyricLinesDrawer.draw(g);
			drawGuitarLanes(g);
			audioDrawer.draw(g);
			drawGuitarNotes(g, highlightData);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
