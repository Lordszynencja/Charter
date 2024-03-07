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
import static log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer.getHighwayDrawer;
import static log.charter.song.notes.ChordOrNote.findPreviousSoundOnString;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.Utils.getStringPosition;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.util.Optional;

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
import log.charter.gui.chartPanelDrawers.instruments.guitar.highway.HighwayDrawer;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.song.Arrangement;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordNote;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.Note;
import log.charter.util.CollectionUtils.ArrayList2;
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
		return x + max(length, noteWidth / 2) >= 0;
	}

	private BeatsDrawer beatsDrawer;
	private ChartPanel chartPanel;
	private KeyboardHandler keyboardHandler;
	private LyricLinesDrawer lyricLinesDrawer;
	private SelectionManager selectionManager;
	private WaveFormDrawer waveFormDrawer;

	public void init(final BeatsDrawer beatsDrawer, final ChartPanel chartPanel, final KeyboardHandler keyboardHandler,
			final LyricLinesDrawer lyricLinesDrawer, final SelectionManager selectionManager,
			final WaveFormDrawer waveFormDrawer) {
		this.beatsDrawer = beatsDrawer;
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

	private void addEventPoints(final Graphics2D g, final int panelWidth, final HighwayDrawer highwayDrawer,
			final Arrangement arrangement, final int time, final HighlightData highlightData) {
		final HashSet2<Integer> selectedIds = selectionManager.getSelectedAccessor(PositionType.EVENT_POINT)//
				.getSelectedSet().map(selection -> selection.id);
		GuitarEventPointsDrawer.addEventPoints(g, panelWidth, highwayDrawer, arrangement.eventPoints,
				arrangement.phrases, time, selectedIds, highlightData);
	}

	private void addToneChanges(final Graphics2D g, final int panelWidth, final HighwayDrawer highwayDrawer,
			final Arrangement arrangement, final int time, final HighlightData highlightData) {
		final HashSet2<Integer> selectedIds = selectionManager.getSelectedAccessor(PositionType.TONE_CHANGE)//
				.getSelectedSet().map(selection -> selection.id);
		GuitarToneChangeDrawer.addToneChanges(g, panelWidth, highwayDrawer, arrangement.baseTone,
				arrangement.toneChanges, time, selectedIds, highlightData);
	}

	private void addAnchors(final Graphics2D g, final int panelWidth, final HighwayDrawer highwayDrawer,
			final Level level, final int time, final HighlightData highlightData) {
		final HashSet2<Integer> selectedIds = getSelectedIds(PositionType.ANCHOR);
		GuitarAnchorsDrawer.addAnchors(g, panelWidth, highwayDrawer, level.anchors, time, selectedIds, highlightData);
	}

	private void drawGuitarLanes(final Graphics g, final int strings, final int time) {
		final int width = chartPanel.getWidth();

		final int x = max(0, timeToX(0, time));

		for (int lane = 0; lane < strings; lane++) {
			g.setColor(getStringBasedColor(StringColorLabelType.LANE, lane, strings));
			final int y = getLaneY(getStringPosition(lane, strings));
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

	private boolean isLinkNextIncorrect(final ArrayList2<ChordOrNote> sounds,
			final ArrayList2<ChordTemplate> chordTemplates, final int id, final int string, final int fret) {
		final ChordOrNote previousSound = findPreviousSoundOnString(string, id - 1, sounds);

		if (previousSound == null) {
			return true;
		}
		if (previousSound.isChord()) {
			final ChordNote chordNote = previousSound.chord().chordNotes.get(string);
			if (chordNote == null) {
				return true;
			}

			if (chordNote.slideTo == null) {
				final ChordTemplate chordTemplate = chordTemplates.get(previousSound.chord().templateId());
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

	private void addGuitarNoteAddLineHighlight(final ArrayList2<ChordTemplate> chordTemplates, final int time,
			final HighwayDrawer highwayDrawer, final HighlightData highlightData) {
		if (highlightData.type != PositionType.GUITAR_NOTE) {
			return;
		}

		for (final HighlightPosition highlightPosition : highlightData.highlightedNonIdPositions) {
			final int x = timeToX(highlightPosition.position, time);
			final Optional<ChordTemplate> template = highlightPosition.originalSound//
					.filter(ChordOrNote::isChord)//
					.map(s -> chordTemplates.get(s.chord().templateId()));
			highwayDrawer.addSoundHighlight(x, highlightPosition.originalSound, template, highlightPosition.string);
		}

		if (highlightData.line.isPresent()) {
			highwayDrawer.addNoteAdditionLine(highlightData.line.get());
		}
	}

	private void addGuitarNotes(final ArrayList2<ChordOrNote> sounds, final ArrayList2<ChordTemplate> chordTemplates,
			final int time, final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final Arrangement arrangement, final int panelWidth) {
		final HashSet2<Integer> selectedNoteIds = getSelectedIds(PositionType.GUITAR_NOTE);
		final int highlightId = highlightData.getId(PositionType.GUITAR_NOTE);

		boolean lastWasLinkNext = false;
		for (int i = 0; i < sounds.size(); i++) {
			final ChordOrNote sound = sounds.get(i);
			final boolean wrongLinkNext;
			if (sound.isNote() && lastWasLinkNext) {
				wrongLinkNext = isLinkNextIncorrect(sounds, chordTemplates, i, sound.note().string, sound.note().fret);
			} else {
				wrongLinkNext = false;
			}

			final boolean selected = selectedNoteIds.contains(i);
			final boolean highlighted = i == highlightId && highlightData.hasStringOf(sound);
			addChordOrNote(time, highwayDrawer, arrangement, panelWidth, sound, selected, highlighted, lastWasLinkNext,
					wrongLinkNext);

			lastWasLinkNext = sound.chord() != null ? sound.chord().linkNext() : sound.note().linkNext;

			if (i == highlightId && !highlighted) {
				final int x = timeToX(sound.position(), time);
				final Optional<ChordTemplate> template = sound.isChord()//
						? Optional.of(chordTemplates.get(sound.chord().templateId()))//
						: Optional.empty();
				highwayDrawer.addSoundHighlight(x, Optional.of(sound), template,
						highlightData.id.get().string.orElse(0));
			}
		}

		addGuitarNoteAddLineHighlight(chordTemplates, time, highwayDrawer, highlightData);
	}

	private void addHandShapes(final int time, final HighwayDrawer highwayDrawer, final HighlightData highlightData,
			final Arrangement arrangement, final Level level, final int panelWidth) {
		final HashSet2<Integer> selectedHandShapeIds = getSelectedIds(PositionType.HAND_SHAPE);
		final int highlightId = highlightData.getId(PositionType.HAND_SHAPE);

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
			final boolean highlighted = i == highlightId;
			highwayDrawer.addHandShape(x, length, selected, highlighted, handShape, chordTemplate);
		}

		if (highlightData.type == PositionType.HAND_SHAPE) {
			highlightData.highlightedNonIdPositions.forEach(
					highlightPosition -> highwayDrawer.addHandShapeHighlight(timeToX(highlightPosition.position, time),
							timeToXLength(highlightPosition.position, highlightPosition.length)));
		}
	}

	public void drawGuitar(final Graphics2D g, final Arrangement arrangement, final Level level, final int time,
			final HighlightData highlightData) {
		final int strings = arrangement.tuning.strings();
		final HighwayDrawer highwayDrawer = getHighwayDrawer(g, strings, time);

		final int panelWidth = chartPanel.getWidth();

		addEventPoints(g, panelWidth, highwayDrawer, arrangement, time, highlightData);
		addToneChanges(g, panelWidth, highwayDrawer, arrangement, time, highlightData);
		addAnchors(g, panelWidth, highwayDrawer, level, time, highlightData);
		drawGuitarLanes(g, arrangement.tuning.strings(), time);
		addGuitarNotes(level.sounds, arrangement.chordTemplates, time, highwayDrawer, highlightData, arrangement,
				panelWidth);
		addHandShapes(time, highwayDrawer, highlightData, arrangement, level, panelWidth);

		highwayDrawer.draw(g);
	}

	public void drawStringNames(final Graphics2D g, final Arrangement arrangement) {
		final int strings = arrangement.tuning.strings();
		final String[] stringNames = arrangement.getSimpleStringNames();

		final int fontSize = (int) (noteHeight * 0.5);
		final Font stringNameFont = new Font(Font.DIALOG, Font.BOLD, fontSize);
		final int x = fontSize * 5 / 6;

		final int width = fontSize * 5 / 3;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(ColorLabel.LANE.color());
		g.fillRect(0, lanesTop, width + 3, lanesHeight + 1);

		for (int string = 0; string < stringNames.length; string++) {
			final int stringPosition = getStringPosition(string, stringNames.length);
			final int y = getLaneY(stringPosition);
			new CenteredText(new Position2D(x, y), stringNameFont, stringNames[string],
					getStringBasedColor(StringColorLabelType.LANE, string, strings)).draw(g);
		}
	}

	public void draw(final Graphics2D g, final Arrangement arrangement, final Level level, final int time,
			final HighlightData highlightData) {
		try {
			waveFormDrawer.draw(g, time);
			beatsDrawer.draw(g, time, highlightData);
			lyricLinesDrawer.draw(g, time);

			drawGuitar(g, arrangement, level, time, highlightData);
			drawStringNames(g, arrangement);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
