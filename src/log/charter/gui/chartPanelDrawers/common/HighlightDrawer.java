package log.charter.gui.chartPanelDrawers.common;

import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneSize;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToLane;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.line;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer.getVocalNotePosition;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.ChordOrNote;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.chartPanelDrawers.instruments.GuitarDrawer;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.gui.handlers.MouseHandler;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.notes.Chord;
import log.charter.song.notes.Note;
import log.charter.util.Position2D;

public class HighlightDrawer {
	private static final Color highlightColor = ColorLabel.HIGHLIGHT.color();
	private static final Color noteAdditionLineColor = ColorLabel.NOTE_ADD_LINE.color();

	private static final int noteWidth = GuitarDrawer.noteWidth + 1;

	private static interface HighlightTypeDrawer {
		void drawHighlight(Graphics g, PositionWithIdAndType highlight, int x, int y);
	}

	private static interface DragTypeDrawer {
		void drawDrag(Graphics g, PositionWithIdAndType highlight, int x);
	}

	private ChartData data;
	private HighlightManager highlightManager;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;

	public void init(final ChartData data, final HighlightManager highlightManager, final ModeManager modeManager,
			final MouseHandler mouseHandler, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager) {
		this.data = data;
		this.highlightManager = highlightManager;
		this.modeManager = modeManager;
		this.mouseHandler = mouseHandler;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.selectionManager = selectionManager;
	}

	private void drawAnchorHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		if (highlight.anchor == null) {
			return;
		}

		final int beatX = timeToX(highlight.anchor.position, data.time);
		final int top = anchorY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(beatX - 1, top, 2, bottom - top);
		strokedRectangle(beatPosition, highlightColor).draw(g);
	}

	private void drawBeatHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
		if (highlight.beat == null) {
			return;
		}

		final int beatX = timeToX(highlight.beat.position, data.time);
		final int top = beatTextY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(beatX - 1, top, 2, bottom - top);
		strokedRectangle(beatPosition, highlightColor).draw(g);
	}

	private void drawNoteHighlight(final Graphics g, final int string, final int position, final int length,
			final int strings) {
		final int x = timeToX(position, data.time);
		final int y = getLaneY(string, strings);
		final int noteHeight = getLaneSize(strings) + 1;
		final ShapePositionWithSize notePosition = new ShapePositionWithSize(x, y, noteWidth, noteHeight)//
				.centered();
		strokedRectangle(notePosition, highlightColor).draw(g);

//		final int tailHeight = getAsOdd((noteHeight - 1) * 3 / 4) + 1;
//		final int tailLength = timeToXLength(length);
//		if (tailLength > 0) {
//			final int tailX = x;
//			final ShapePositionWithSize tailPosition = new ShapePositionWithSize(tailX, y, tailLength + 2, tailHeight)//
//					.centeredY();
//			 strokedRectangle(tailPosition, highlightColor).draw(g);
//		}
	}

	private void drawChordHighlight(final Graphics g, final Chord chord, final ChordTemplate chordTemplate,
			final int position, final int strings) {
		for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
			drawNoteHighlight(g, chordFret.getKey(), position, chord.length, strings);
		}
	}

	private void drawGuitarNoteHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		final int strings = data.getCurrentArrangement().tuning.strings;

		if (highlight.chordOrNote == null) {
			final int lane = yToLane(y, strings);
			drawNoteHighlight(g, lane, highlight.position, 0, strings);

			return;
		}

		if (highlight.chordOrNote.chord != null) {
			final Chord chord = highlight.chordOrNote.chord;
			final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.chordId);
			drawChordHighlight(g, chord, chordTemplate, chord.position, strings);

			return;
		}

		if (highlight.chordOrNote.note != null) {
			final Note note = highlight.chordOrNote.note;
			drawNoteHighlight(g, note.string, note.position, note.length, strings);

			return;
		}
	}

	private ShapePositionWithSize getHandShapeHighlightPosition(final PositionWithIdAndType highlight) {
		if (highlight.handShape == null) {
			final int x = timeToX(highlight.position, data.time);
			return new ShapePositionWithSize(x, lanesBottom, 50, 10);
		}

		final HandShape handShape = highlight.handShape;
		final int x = timeToX(handShape.position, data.time);
		final int length = timeToXLength(handShape.length);
		return new ShapePositionWithSize(x, lanesBottom, length, 10);
	}

	private void drawHandShapeHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		strokedRectangle(getHandShapeHighlightPosition(highlight), highlightColor).draw(g);
	}

	private void drawNoneHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
	}

	private void drawVocalHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
		final int position = highlight.position;
		final int length = highlight.vocal == null ? 50 : highlight.vocal.length;
		final ShapePositionWithSize vocalNotePosition = getVocalNotePosition(position, length, data.time);
		strokedRectangle(vocalNotePosition.resized(-1, -1, 1, 1), highlightColor).draw(g);
	}

	private final Map<PositionType, HighlightTypeDrawer> highlightDrawers = new HashMap<>();

	{
		highlightDrawers.put(PositionType.ANCHOR, this::drawAnchorHighlight);
		highlightDrawers.put(PositionType.BEAT, this::drawBeatHighlight);
		highlightDrawers.put(PositionType.GUITAR_NOTE, this::drawGuitarNoteHighlight);
		highlightDrawers.put(PositionType.HAND_SHAPE, this::drawHandShapeHighlight);
		highlightDrawers.put(PositionType.NONE, this::drawNoneHighlight);
		highlightDrawers.put(PositionType.VOCAL, this::drawVocalHighlight);
	}

	private void drawAnchorDrag(final Graphics g, final PositionWithIdAndType highlight, final int x) {
		if (highlight.anchor == null && !selectionManager.getSelectedAccessor(PositionType.ANCHOR).isSelected()) {
			return;
		}

		final int position = data.songChart.beatsMap.getPositionFromGridClosestTo(xToTime(x, data.time));
		final int dragX = timeToX(position, data.time);
		lineVertical(dragX, anchorY, lanesBottom, highlightColor).draw(g);
	}

	private void drawBeatDrag(final Graphics g, final PositionWithIdAndType highlight, final int x) {
		if (highlight.beat == null && !selectionManager.getSelectedAccessor(PositionType.BEAT).isSelected()) {
			return;
		}

		lineVertical(x, beatTextY, lanesBottom, highlightColor).draw(g);
	}

	private void drawGuitarNoteDrag(final Graphics g, final PositionWithIdAndType highlight, final int x) {
		if (highlight.chordOrNote == null//
				&& !selectionManager.getSelectedAccessor(PositionType.GUITAR_NOTE).isSelected()) {
			return;
		}

		final int strings = data.getCurrentArrangement().tuning.strings;

		if (highlight.chordOrNote != null) {
			final ChordOrNote chordOrNote = highlight.chordOrNote;
			final int position = data.songChart.beatsMap.getPositionFromGridClosestTo(xToTime(x, data.time));
			if (chordOrNote.chord != null) {
				final Chord chord = chordOrNote.chord;
				final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.chordId);

				drawChordHighlight(g, chord, chordTemplate, position, strings);
				return;
			}

			drawNoteHighlight(g, chordOrNote.note.string, position, chordOrNote.note.length, strings);
			return;
		}
	}

	private void drawHandShapeDrag(final Graphics g, final PositionWithIdAndType highlight, final int x) {
		if (highlight.anchor == null) {
			return;
		}

		final int position = data.songChart.beatsMap.getPositionFromGridClosestTo(xToTime(x, data.time));
		final int dragX = timeToX(position, data.time);
		lineVertical(dragX, anchorY, lanesBottom, highlightColor).draw(g);
	}

	private void drawNoneDrag(final Graphics g, final PositionWithIdAndType highlight, final int x) {
	}

	private void drawVocalDrag(final Graphics g, final PositionWithIdAndType highlight, final int x) {
		if (highlight.anchor == null) {
			return;
		}

		final int position = data.songChart.beatsMap.getPositionFromGridClosestTo(xToTime(x, data.time));
		final int dragX = timeToX(position, data.time);
		lineVertical(dragX, anchorY, lanesBottom, highlightColor).draw(g);
	}

	private final Map<PositionType, DragTypeDrawer> dragDrawers = new HashMap<>();

	{
		dragDrawers.put(PositionType.ANCHOR, this::drawAnchorDrag);
		dragDrawers.put(PositionType.BEAT, this::drawBeatDrag);
		dragDrawers.put(PositionType.GUITAR_NOTE, this::drawGuitarNoteDrag);
		dragDrawers.put(PositionType.HAND_SHAPE, this::drawHandShapeDrag);
		dragDrawers.put(PositionType.NONE, this::drawNoneDrag);
		dragDrawers.put(PositionType.VOCAL, this::drawVocalDrag);
	}

	private void drawNoteAdditionHighlight(final Graphics g, final int x, final int y) {
		if (modeManager.editMode == EditMode.VOCALS) {
			return;
		}

		final MouseButtonPressData pressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.RIGHT_BUTTON);
		if (pressPosition == null) {
			return;
		}

		final int pressXTime = pressPosition.highlight.position;
		final int pressX = timeToX(pressXTime, data.time);
		final int pressY = pressPosition.position.y;

		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		final int currentX = timeToX(highlight.position, data.time);
		line(new Position2D(pressX, pressY), new Position2D(currentX, y), noteAdditionLineColor).draw(g);

//		final ArrayList2<PositionWithStringOrNoteId> positions = highlightManager.getPositionsWithStrings(pressXTime,
//				highlight.position, pressY, y);
//		ArrayList2<ChordOrNote> chordsAndNotes = PositionType.GUITAR_NOTE.getPositions(data);
//		final int strings = data.getCurrentArrangement().tuning.strings;
//
//		for (final PositionWithStringOrNoteId highlightPosition : positions) {
//			if (highlightPosition.chordId != null) {
//				final chordOrNote chordOrNote = chordsAndNotes.get(highlightPosition.chordId);
//				final PositionWithIdAndType positionHighlight = PositionWithIdAndType.create(highlightPosition.chordId,
//						chord);
//				drawGuitarNoteHighlight(g, positionHighlight, currentX, y);
//				continue;
//			}
//			if (highlightPosition.noteId != null) {
//				final Note note = notes.get(highlightPosition.noteId);
//				final PositionWithIdAndType positionHighlight = PositionWithIdAndType.create(highlightPosition.noteId,
//						note);
//				drawGuitarNoteHighlight(g, positionHighlight, currentX, y);
//				continue;
//			}
//
//			final PositionWithIdAndType positionHighlight = PositionWithIdAndType.create(highlightPosition.position,
//					PositionType.GUITAR_NOTE);
//			final int laneY = getLaneY(highlightPosition.string, strings);
//			drawGuitarNoteHighlight(g, positionHighlight, currentX, laneY);
//		}
	}

	public void draw(final Graphics g) {
		final MouseButtonPressData leftPressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.LEFT_BUTTON);
		if (leftPressPosition != null) {
			final DragTypeDrawer drawer = dragDrawers.get(leftPressPosition.highlight.type);
			drawer.drawDrag(g, leftPressPosition.highlight, mouseHandler.getMouseX());
			return;
		}

		final int x = mouseHandler.getMouseX();
		final int y = mouseHandler.getMouseY();
		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		highlightDrawers.get(highlight.type).drawHighlight(g, highlight, x, y);

		drawNoteAdditionHighlight(g, x, y);
	}
}
