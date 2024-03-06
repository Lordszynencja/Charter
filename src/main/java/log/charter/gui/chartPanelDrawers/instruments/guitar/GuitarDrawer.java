package log.charter.gui.chartPanelDrawers.instruments.guitar;

import static java.lang.Math.max;
import static log.charter.data.config.Config.showChordIds;
import static log.charter.data.config.GraphicalConfig.noteHeight;
import static log.charter.data.config.GraphicalConfig.noteWidth;
import static log.charter.gui.ChartPanelColors.getStringBasedColor;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesHeight;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesTop;
import static log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData.fromChord;
import static log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData.fromNote;
import static log.charter.gui.chartPanelDrawers.instruments.guitar.HighwayDrawer.getHighwayDrawer;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.gui.ChartPanel;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.ChartPanelColors.StringColorLabelType;
import log.charter.gui.chartPanelDrawers.common.BeatsDrawer;
import log.charter.gui.chartPanelDrawers.common.LyricLinesDrawer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.chartPanelDrawers.data.EditorNoteDrawingData;
import log.charter.gui.chartPanelDrawers.data.HighlightData;
import log.charter.gui.chartPanelDrawers.data.HighlightData.HighlightPosition;
import log.charter.gui.chartPanelDrawers.drawableShapes.CenteredText;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.song.Anchor;
import log.charter.song.Arrangement;
import log.charter.song.ChordTemplate;
import log.charter.song.EventPoint;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Phrase;
import log.charter.song.ToneChange;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;

public class GuitarDrawer {
	public static void reloadGraphics() {
		HighwayDrawer.reloadGraphics();
	}

	public static final BigDecimal bendStepSize = new BigDecimal("10");

	private static boolean isPastRightEdge(final int x, final int width) {
		return x > (width + noteWidth / 2);
	}

	private static boolean isOnScreen(final int x, final int length) {
		return x + length >= 0;
	}

	private BeatsDrawer beatsDrawer;
	private ChartData data;
	private ChartPanel chartPanel;
	private KeyboardHandler keyboardHandler;
	private LyricLinesDrawer lyricLinesDrawer;
	private SelectionManager selectionManager;
	private WaveFormDrawer waveFormDrawer;

	public void init(final BeatsDrawer beatsDrawer, final ChartData data, final ChartPanel chartPanel,
			final KeyboardHandler keyboardHandler, final LyricLinesDrawer lyricLinesDrawer,
			final SelectionManager selectionManager, final WaveFormDrawer waveFormDrawer) {
		this.beatsDrawer = beatsDrawer;
		this.data = data;
		this.chartPanel = chartPanel;
		this.keyboardHandler = keyboardHandler;
		this.lyricLinesDrawer = lyricLinesDrawer;
		this.selectionManager = selectionManager;
		this.waveFormDrawer = waveFormDrawer;
	}

	private HashSet2<Integer> getSelectedIds(final PositionType positionType) {
		return selectionManager.getSelectedAccessor(positionType)//
				.getSelectedSet().map(selection -> selection.id);
	}

	private void addEventPoints(final int time, final HighwayDrawer highwayDrawer, final HighlightData highlightData) {
		final HashSet2<Integer> selectedEventPointIds = selectionManager.getSelectedAccessor(PositionType.EVENT_POINT)//
				.getSelectedSet().map(selection -> selection.id);
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.EVENT_POINT
				&& highlightData.highlightedId != null;

		final ArrayList2<EventPoint> eventPoints = data.getCurrentArrangement().eventPoints;
		final HashMap2<String, Phrase> phrases = data.getCurrentArrangement().phrases;

		for (int i = 0; i < eventPoints.size(); i++) {
			final EventPoint eventPoint = eventPoints.get(i);
			final int x = timeToX(eventPoint.position(), time);
			if (x < -1000) {
				continue;
			}
			if (x > chartPanel.getWidth()) {
				break;
			}

			final boolean selected = selectedEventPointIds.contains(i);
			final boolean highlighted = canBeHighlighted && i == highlightData.highlightedId;
			highwayDrawer.addEventPoint(eventPoint, phrases.get(eventPoint.phrase), x, selected, highlighted);
		}

		if (highlightData.highlightType == PositionType.EVENT_POINT) {
			highlightData.highlightedNonIdPositions.forEach(highlightPosition -> highwayDrawer
					.addEventPointHighlight(timeToX(highlightPosition.position, time)));
		}
	}

	private void addToneChanges(final int time, final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final Arrangement arrangement, final int panelWidth) {
		final HashSet2<Integer> selectedToneChangeIds = getSelectedIds(PositionType.TONE_CHANGE);
		final ArrayList2<ToneChange> toneChanges = arrangement.toneChanges;
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.TONE_CHANGE
				&& highlightData.highlightedId != null;

		for (int i = 0; i < toneChanges.size(); i++) {
			final ToneChange toneChange = toneChanges.get(i);
			final int x = timeToX(toneChange.position(), time);
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
					.addToneChangeHighlight(timeToX(highlightPosition.position, time)));
		}
	}

	private void addAnchors(final int time, final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedAnchorIds = getSelectedIds(PositionType.ANCHOR);
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.ANCHOR
				&& highlightData.highlightedId != null;

		for (int i = 0; i < level.anchors.size(); i++) {
			final Anchor anchor = level.anchors.get(i);
			final int x = timeToX(anchor.position(), time);
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
			highlightData.highlightedNonIdPositions.forEach(
					highlightPosition -> highwayDrawer.addAnchorHighlight(timeToX(highlightPosition.position, time)));
		}
	}

	private void drawGuitarLanes(final Graphics g, final int time) {
		final int lanes = data.getCurrentArrangement().tuning.strings();
		final int width = chartPanel.getWidth();

		final int x = max(0, timeToX(0, time));

		for (int i = 0; i < lanes; i++) {
			g.setColor(getStringBasedColor(StringColorLabelType.LANE, i, lanes));
			final int y = getLaneY(getStringPosition(i, lanes));
			g.drawLine(x, y, width, y);
		}
	}

	private boolean addChord(final int time, final HighwayDrawer highwayDrawer, final Arrangement arrangement,
			final int panelWidth, final Chord chord, final boolean selected, final boolean highlighted,
			final boolean lastWasLinkNext, final boolean wrongLinkNext) {
		final int x = timeToX(chord.position(), time);
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		final int length = timeToXLength(chord.position(), chord.length());
		if (!isOnScreen(x, length)) {
			return true;
		}

		final ChordTemplate chordTemplate = arrangement.chordTemplates.get(chord.templateId());
		for (final EditorNoteDrawingData noteData : fromChord(chord, chordTemplate, x, selected, highlighted,
				lastWasLinkNext, wrongLinkNext, keyboardHandler.ctrl())) {
			highwayDrawer.addNote(noteData);
		}

		String chordName = chordTemplate.chordName;
		if (showChordIds) {
			chordName = (chordName == null || chordName.isBlank()) ? "[" + chord.templateId() + "]"
					: chordName + " [" + chord.templateId() + "]";
		}
		if (chordName != null) {
			highwayDrawer.addChordName(x, chordName);

		}

		return true;
	}

	private boolean addNote(final int time, final HighwayDrawer highwayDrawer, final int panelWidth, final Note note,
			final boolean selected, final boolean highlighted, final boolean lastWasLinkNext,
			final boolean wrongLinkNext) {
		final int x = timeToX(note.position(), time);
		final int length = timeToXLength(note.position(), note.length());
		if (isPastRightEdge(x, panelWidth)) {
			return false;
		}

		if (!isOnScreen(x, length)) {
			return true;
		}

		highwayDrawer.addNote(fromNote(x, note, selected, highlighted, lastWasLinkNext, wrongLinkNext));

		return true;
	}

	private boolean addChordOrNote(final int time, final HighwayDrawer highwayDrawer, final Arrangement arrangement,
			final int panelWidth, final ChordOrNote chordOrNote, final boolean selected, final boolean highlighted,
			final boolean lastWasLinkNext, final boolean wrongLinkNext) {
		if (chordOrNote.isChord()) {
			return addChord(time, highwayDrawer, arrangement, panelWidth, chordOrNote.chord(), selected, highlighted,
					lastWasLinkNext, wrongLinkNext);
		}
		if (chordOrNote.isNote()) {
			return addNote(time, highwayDrawer, panelWidth, chordOrNote.note(), selected, highlighted, lastWasLinkNext,
					wrongLinkNext);
		}

		return true;
	}

	private boolean isLinkNextIncorrect(final int id, final int string, final int fret) {
		final ChordOrNote previousSound = ChordOrNote.findPreviousSoundOnString(string, id - 1,
				data.getCurrentArrangementLevel().sounds);

		if (previousSound == null) {
			return true;
		}
		if (previousSound.isChord()) {
			final ChordNote chordNote = previousSound.chord().chordNotes.get(string);
			if (chordNote == null) {
				return true;
			}

			if (chordNote.slideTo == null) {
				final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates
						.get(previousSound.chord().templateId());
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
			if (previousSound.note().string != string) {
				return true;
			}
			if (previousSound.note().slideTo == null) {
				if (previousSound.note().fret != fret) {
					return true;
				}
			} else {
				if (previousSound.note().unpitchedSlide) {
					return true;
				}
				if (previousSound.note().slideTo != fret) {
					return true;
				}
			}
		}

		return false;
	}

	private void addGuitarNotes(final int time, final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final Arrangement arrangement, final int panelWidth) {
		final HashSet2<Integer> selectedNoteIds = getSelectedIds(PositionType.GUITAR_NOTE);
		final ArrayList2<ChordOrNote> chordsAndNotes = data.getCurrentArrangementLevel().sounds;
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.GUITAR_NOTE
				&& highlightData.highlightedId != null;

		boolean lastWasLinkNext = false;
		for (int i = 0; i < chordsAndNotes.size(); i++) {
			final ChordOrNote sound = chordsAndNotes.get(i);
			final boolean wrongLinkNext;
			if (sound.isNote() && lastWasLinkNext) {
				wrongLinkNext = isLinkNextIncorrect(i, sound.note().string, sound.note().fret);
			} else {
				wrongLinkNext = false;
			}

			final boolean selected = selectedNoteIds.contains(i);
			final boolean highlighted = canBeHighlighted && i == highlightData.highlightedId;
			addChordOrNote(time, highwayDrawer, arrangement, panelWidth, sound, selected, highlighted, lastWasLinkNext,
					wrongLinkNext);

			lastWasLinkNext = sound.chord() != null ? sound.chord().linkNext() : sound.note().linkNext;
		}

		if (highlightData.highlightType == PositionType.GUITAR_NOTE) {
			final ArrayList2<ChordTemplate> templates = data.getCurrentArrangement().chordTemplates;
			for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
				final int x = timeToX(highlightPosition.position, time);
				final ChordOrNote sound = highlightPosition.originalSound;
				final ChordTemplate template = sound != null && sound.isChord()
						? templates.get(sound.chord().templateId())
						: null;
				highwayDrawer.addSoundHighlight(x, sound, template, highlightPosition.string);
			}
		}

		if (highlightData.highlightLineStart != null && highlightData.highlightLineEnd != null) {
			highwayDrawer.addNoteAdditionLine(highlightData.highlightLineStart, highlightData.highlightLineEnd);
		}
	}

	private void addHandShapes(final int time, final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final Arrangement arrangement, final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedHandShapeIds = getSelectedIds(PositionType.HAND_SHAPE);
		final boolean canBeHighlighted = highlightData.highlightType == PositionType.HAND_SHAPE
				&& highlightData.highlightedId != null;

		for (int i = 0; i < level.handShapes.size(); i++) {
			final HandShape handShape = level.handShapes.get(i);
			final int x = timeToX(handShape.position(), time);
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
			highlightData.highlightedNonIdPositions.forEach(
					highlightPosition -> highwayDrawer.addHandShapeHighlight(timeToX(highlightPosition.position, time),
							timeToXLength(highlightPosition.position, highlightPosition.length)));
		}
	}

	public void drawGuitar(final Graphics g, final int time, final HighlightData highlightData) {
		final Level level = data.getCurrentArrangementLevel();
		final Arrangement arrangement = data.getCurrentArrangement();
		final int strings = data.getCurrentArrangement().tuning.strings();
		final HighwayDrawer highwayDrawer = getHighwayDrawer(g, strings, time);

		final int panelWidth = chartPanel.getWidth();

		addEventPoints(time, highwayDrawer, highlightData);
		addToneChanges(time, highwayDrawer, highlightData, arrangement, panelWidth);
		addAnchors(time, highwayDrawer, highlightData, level, panelWidth);
		drawGuitarLanes(g, time);
		addGuitarNotes(time, highwayDrawer, highlightData, arrangement, panelWidth);
		addHandShapes(time, highwayDrawer, highlightData, arrangement, level, panelWidth);

		highwayDrawer.draw(g);
	}

	public void drawStringNames(final Graphics g) {
		final String[] stringNames = data.getCurrentArrangement().getSimpleStringNames();

		final int fontSize = (int) (noteHeight * 0.5);
		final Font stringNameFont = new Font(Font.DIALOG, Font.BOLD, fontSize);
		final int x = fontSize * 5 / 6;

		final int width = fontSize * 5 / 3;

		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(ColorLabel.LANE.color());
		g.fillRect(0, lanesTop, width + 3, lanesHeight + 1);

		final int lanes = data.currentStrings();
		for (int string = 0; string < stringNames.length; string++) {
			final int stringPosition = getStringPosition(string, stringNames.length);
			final int y = getLaneY(stringPosition);
			new CenteredText(new Position2D(x, y), stringNameFont, stringNames[string],
					getStringBasedColor(StringColorLabelType.LANE, string, lanes)).draw(g);
		}
	}

	public void draw(final Graphics g, final int time, final HighlightData highlightData) {
		try {
			waveFormDrawer.draw(g, time);
			beatsDrawer.draw(g, time, highlightData);
			lyricLinesDrawer.draw(g, time);
			drawGuitar(g, time, highlightData);
			drawStringNames(g);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
