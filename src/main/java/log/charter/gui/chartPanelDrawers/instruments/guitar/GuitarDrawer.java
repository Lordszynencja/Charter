package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static log.charter.data.config.Config.noteWidth;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;

import java.awt.Graphics;
import java.math.BigDecimal;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.song.Anchor;
import log.charter.song.ArrangementChart;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.ToneChange;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class GuitarDrawer {
	public static final BigDecimal bendStepSize = new BigDecimal("10");

	private static boolean isPastRightEdge(final int x, final int width) {
		return x > (width + noteWidth / 2);
	}

	private static boolean isOnScreen(final int x, final int length) {
		return x + length >= 0;
	}

	private AudioDrawer audioDrawer;
	private BeatsDrawer beatsDrawer;
	protected ChartData data;
	private ChartPanel chartPanel;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;

	public void init(final AudioDrawer audioDrawer, final BeatsDrawer beatsDrawer, final ChartData data,
			final ChartPanel chartPanel, final KeyboardHandler keyboardHandler,
			final SelectionManager selectionManager) {
		this.audioDrawer = audioDrawer;
		this.beatsDrawer = beatsDrawer;
		this.data = data;
		this.chartPanel = chartPanel;
		this.keyboardHandler = keyboardHandler;
		this.selectionManager = selectionManager;
	}

	private void drawGuitarLanes(final Graphics g) {
		final int lanes = data.getCurrentArrangement().tuning.strings;
		final int width = chartPanel.getWidth();

		final int x = timeToX(0, data.time);

		for (int i = 0; i < lanes; i++) {
			g.setColor(ColorLabel.valueOf("LANE_" + i).color());
			final int y = getLaneY(i, lanes);
			g.drawLine(x, y, width, y);
		}
	}

	private HashSet2<Integer> getSelectedIds(final PositionType positionType) {
		return selectionManager.getSelectedAccessor(positionType)//
				.getSelectedSet().map(selection -> selection.id);
	}

	private void addToneChanges(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement,
			final int panelWidth) {
		final HashSet2<Integer> selectedToneChangeIds = getSelectedIds(PositionType.TONE_CHANGE);
		final ArrayList2<ToneChange> toneChanges = arrangement.toneChanges;

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
			highwayDrawer.addToneChange(toneChange, x, selected);
		}
	}

	private void addAnchors(final HighwayDrawer highwayDrawer, final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedAnchorIds = getSelectedIds(PositionType.ANCHOR);

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
			highwayDrawer.addAnchor(anchor, x, selected);
		}
	}

	private boolean addChord(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement,
			final int panelWidth, final Chord chord, final boolean selected, final boolean lastWasLinkNext) {
		final int x = timeToX(chord.position(), data.time);
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		final int length = timeToXLength(chord.length());
		if (!isOnScreen(x, length)) {
			return true;
		}

		final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.chordId);
		highwayDrawer.addChord(chord, chordTemplate, x, length, selected, lastWasLinkNext, keyboardHandler.ctrl());
		return true;
	}

	private boolean addNote(final HighwayDrawer highwayDrawer, final int panelWidth, final Note note,
			final boolean selected, final boolean lastWasLinkNext) {
		final int x = timeToX(note.position(), data.time);
		final int length = timeToXLength(note.length());
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		if (!isOnScreen(x, length)) {
			return true;
		}

		highwayDrawer.addNote(note, x, length, selected, lastWasLinkNext);

		return true;
	}

	private boolean addChordOrNote(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement,
			final int panelWidth, final ChordOrNote chordOrNote, final boolean selected,
			final boolean lastWasLinkNext) {
		if (chordOrNote.chord != null) {
			return addChord(highwayDrawer, arrangement, panelWidth, chordOrNote.chord, selected, lastWasLinkNext);
		}
		if (chordOrNote.note != null) {
			return addNote(highwayDrawer, panelWidth, chordOrNote.note, selected, lastWasLinkNext);
		}

		return true;
	}

	private void addGuitarNotes(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement,
			final int panelWidth) {
		final HashSet2<Integer> selectedNoteIds = getSelectedIds(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordOrNote> chordsAndNotes = data.getCurrentArrangementLevel().chordsAndNotes;

		boolean lastWasLinkNext = false;
		for (int i = 0; i < chordsAndNotes.size(); i++) {
			final ChordOrNote chordOrNote = chordsAndNotes.get(i);
			final boolean selected = selectedNoteIds.contains(i);
			addChordOrNote(highwayDrawer, arrangement, panelWidth, chordOrNote, selected, lastWasLinkNext);

			lastWasLinkNext = chordOrNote.chord != null ? chordOrNote.chord.linkNext : chordOrNote.note.linkNext;
		}
	}

	private void addHandShapes(final HighwayDrawer highwayDrawer, final ArrangementChart arrangement, final Level level,
			final int panelWidth) {
		final HashSet2<Integer> selectedHandShapeIds = getSelectedIds(PositionType.HAND_SHAPE);

		for (int i = 0; i < level.handShapes.size(); i++) {
			final HandShape handShape = level.handShapes.get(i);
			final int x = timeToX(handShape.position(), data.time);
			if (isPastRightEdge(x, panelWidth)) {
				break;
			}

			final int length = timeToXLength(handShape.length());
			if (!isOnScreen(x, length)) {
				continue;
			}

			final ChordTemplate chordTemplate;
			if (handShape.chordId >= 0 && arrangement.chordTemplates.size() > handShape.chordId) {
				chordTemplate = arrangement.chordTemplates.get(handShape.chordId);
			} else {
				chordTemplate = new ChordTemplate();
			}
			final boolean selected = selectedHandShapeIds.contains(i);
			highwayDrawer.addHandShape(x, length, selected, handShape, chordTemplate);
		}
	}

	private void drawGuitarNotes(final Graphics g) {
		final Level level = data.getCurrentArrangementLevel();
		final ArrangementChart arrangement = data.getCurrentArrangement();
		final int strings = data.getCurrentArrangement().tuning.strings;
		final HighwayDrawer highwayDrawer = HighwayDrawer.getHighwayDrawer(strings, data.time);

		final int panelWidth = chartPanel.getWidth();

		addToneChanges(highwayDrawer, arrangement, panelWidth);
		addAnchors(highwayDrawer, level, panelWidth);
		addGuitarNotes(highwayDrawer, arrangement, panelWidth);
		addHandShapes(highwayDrawer, arrangement, level, panelWidth);

		highwayDrawer.draw(g);
	}

	public void draw(final Graphics g) {
		try {
			beatsDrawer.draw(g);
			drawGuitarLanes(g);
			audioDrawer.draw(g);
			drawGuitarNotes(g);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
