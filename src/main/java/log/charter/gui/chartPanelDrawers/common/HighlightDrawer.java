package log.charter.gui.chartPanelDrawers.common;

import static java.lang.Math.abs;
import static log.charter.data.config.Config.noteHeight;
import static log.charter.data.config.Config.noteWidth;
import static log.charter.data.types.PositionType.BEAT;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.anchorY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.beatTextY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.getLaneY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.lanesBottom;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.sectionNamesY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.toneChangeY;
import static log.charter.gui.chartPanelDrawers.common.DrawerUtils.yToLane;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.line;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.lineVertical;
import static log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape.strokedRectangle;
import static log.charter.gui.chartPanelDrawers.instruments.VocalsDrawer.getVocalNotePosition;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.timeToXLength;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.Graphics;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import log.charter.data.ChartData;
import log.charter.data.managers.HighlightManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.PositionWithStringOrNoteId;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.ChartPanelColors.ColorLabel;
import log.charter.gui.chartPanelDrawers.drawableShapes.DrawableShape;
import log.charter.gui.chartPanelDrawers.drawableShapes.ShapePositionWithSize;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;
import log.charter.gui.handlers.MouseHandler;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.Position;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.Position2D;

public class HighlightDrawer {
	private static interface HighlightTypeDrawer {
		void drawHighlight(Graphics g, PositionWithIdAndType highlight, int x, int y);
	}

	private static interface DragTypeDrawer {
		void drawDrag(Graphics g, MouseButtonPressData press, int x);
	}

	private static class CopiedPositionData<T extends IPosition> implements IPosition {
		public final IPosition position;
		public final T object;

		public CopiedPositionData(final T object) {
			this.position = new Position(object);
			this.object = object;
		}

		@Override
		public int position() {
			return position.position();
		}

		@Override
		public void position(final int newPosition) {
			position.position(newPosition);
		}
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
		final int anchorX = timeToX(highlight.position(), data.time);
		final int top = anchorY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(anchorX - 1, top, 2, bottom - top);
		strokedRectangle(beatPosition, ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private void drawBeatHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
		if (highlight.beat == null) {
			return;
		}

		final int beatX = timeToX(highlight.beat.position(), data.time);
		final int top = beatTextY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(beatX - 1, top, 2, bottom - top);
		strokedRectangle(beatPosition, ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private void drawEventPointHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		final int eventPointX = timeToX(highlight.position(), data.time);
		final int top = sectionNamesY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize eventPointPosition = new ShapePositionWithSize(eventPointX - 1, top, 2,
				bottom - top);
		strokedRectangle(eventPointPosition, ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private void drawNoteHighlight(final Graphics g, final int string, final int position, final int strings) {
		final int x = timeToX(position, data.time);
		final int y = getLaneY(string, strings);
		final ShapePositionWithSize notePosition = new ShapePositionWithSize(x, y, noteWidth, noteHeight)//
				.centered()//
				.resized(-1, -1, 1, 1);
		strokedRectangle(notePosition, ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private void drawChordHighlight(final Graphics g, final Chord chord, final ChordTemplate chordTemplate,
			final int position, final int strings) {
		for (final Entry<Integer, Integer> chordFret : chordTemplate.frets.entrySet()) {
			drawNoteHighlight(g, chordFret.getKey(), position, strings);
		}
	}

	private void drawGuitarNoteHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		final int strings = data.getCurrentArrangement().tuning.strings;
		final int lane = yToLane(y, strings);
		drawNoteHighlight(g, lane, highlight.position(), strings);
	}

	private ShapePositionWithSize getHandShapeHighlightPosition(final PositionWithIdAndType highlight) {
		if (highlight.handShape == null) {
			final int x = timeToX(highlight.position(), data.time);
			return new ShapePositionWithSize(x, lanesBottom, 50, 10);
		}

		final HandShape handShape = highlight.handShape;
		final int x = timeToX(handShape.position(), data.time);
		final int length = timeToXLength(handShape.length());
		return new ShapePositionWithSize(x, lanesBottom, length, 10);
	}

	private void drawHandShapeHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		strokedRectangle(getHandShapeHighlightPosition(highlight), ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private void drawNoneHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
	}

	private void drawToneChangeHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x,
			final int y) {
		final int toneChangeX = timeToX(highlight.position(), data.time);
		final int top = toneChangeY - 1;
		final int bottom = lanesBottom + 1;
		final ShapePositionWithSize beatPosition = new ShapePositionWithSize(toneChangeX - 1, top, 2, bottom - top);
		strokedRectangle(beatPosition, ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private void drawVocalHighlight(final Graphics g, final PositionWithIdAndType highlight, final int x, final int y) {
		final int position = highlight.position();
		final int length = highlight.vocal == null ? 50 : highlight.vocal.length();
		final ShapePositionWithSize vocalNotePosition = getVocalNotePosition(position, length, data.time);
		strokedRectangle(vocalNotePosition.resized(-1, -1, 1, 1), ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private final Map<PositionType, HighlightTypeDrawer> highlightDrawers = new HashMap<>();

	{
		highlightDrawers.put(PositionType.ANCHOR, this::drawAnchorHighlight);
		highlightDrawers.put(PositionType.BEAT, this::drawBeatHighlight);
		highlightDrawers.put(PositionType.EVENT_POINT, this::drawEventPointHighlight);
		highlightDrawers.put(PositionType.GUITAR_NOTE, this::drawGuitarNoteHighlight);
		highlightDrawers.put(PositionType.HAND_SHAPE, this::drawHandShapeHighlight);
		highlightDrawers.put(PositionType.NONE, this::drawNoneHighlight);
		highlightDrawers.put(PositionType.TONE_CHANGE, this::drawToneChangeHighlight);
		highlightDrawers.put(PositionType.VOCAL, this::drawVocalHighlight);
	}

	private void moveSelectedPositions(final Collection<? extends IPosition> positions,
			final MouseButtonPressData press, final int x) {
		final int dragFrom = press.highlight.position();
		final int dragTo = data.songChart.beatsMap.getPositionFromGridClosestTo(xToTime(x, data.time));
		data.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);
	}

	private void drawSimpleDraggedHighlight(final Graphics g, final MouseButtonPressData press, final int x,
			final Function<Integer, DrawableShape> shapeGenerator) {
		final HashSet2<IPosition> positions = selectionManager.getSelectedAccessor(press.highlight.type)
				.getSelectedSet()//
				.map(selection -> new Position(selection.selectable));
		if (positions.isEmpty()) {
			return;
		}

		moveSelectedPositions(positions, press, x);

		positions.forEach(position -> shapeGenerator.apply(timeToX(position, data.time)).draw(g));
	}

	private DrawableShape makeDraggedAnchorShape(final int x) {
		return lineVertical(x, anchorY, lanesBottom, ColorLabel.HIGHLIGHT.color());
	}

	private void drawAnchorDrag(final Graphics g, final MouseButtonPressData press, final int x) {
		drawSimpleDraggedHighlight(g, press, x, this::makeDraggedAnchorShape);
	}

	private void drawBeatDrag(final Graphics g, final MouseButtonPressData press, final int x) {
		if (press.highlight.beat == null) {
			return;
		}

		lineVertical(x, beatTextY, lanesBottom, ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private DrawableShape makeDraggedEventPointShape(final int x) {
		return lineVertical(x, sectionNamesY, lanesBottom, ColorLabel.HIGHLIGHT.color());
	}

	private void drawEventPointDrag(final Graphics g, final MouseButtonPressData press, final int x) {
		drawSimpleDraggedHighlight(g, press, x, this::makeDraggedEventPointShape);
	}

	private void drawSoundDrag(final Graphics g, final int strings, final int position, final ChordOrNote sound) {
		if (sound.isNote()) {
			drawNoteHighlight(g, sound.note.string, position, strings);
		} else {
			final Chord chord = sound.chord;
			final ChordTemplate chordTemplate = data.getCurrentArrangement().chordTemplates.get(chord.templateId());

			drawChordHighlight(g, chord, chordTemplate, position, strings);
		}
	}

	private void drawGuitarNoteDrag(final Graphics g, final MouseButtonPressData press, final int x) {
		final HashSet2<CopiedPositionData<ChordOrNote>> positions = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE).getSelectedSet()//
				.map(selection -> new CopiedPositionData<>((ChordOrNote) selection.selectable));
		if (positions.isEmpty()) {
			return;
		}

		final int strings = data.currentStrings();
		moveSelectedPositions(positions, press, x);
		for (final CopiedPositionData<ChordOrNote> position : positions) {
			drawSoundDrag(g, strings, position.position(), position.object);
		}
	}

	private DrawableShape makeDraggedHandShapeShape(final CopiedPositionData<HandShape> position) {
		final int drawX = timeToX(position.position(), data.time);
		final int length = timeToXLength(position.object.length());
		final ShapePositionWithSize handShapeDragHighlightPosition = new ShapePositionWithSize(drawX, lanesBottom,
				length, 10);
		return strokedRectangle(handShapeDragHighlightPosition, ColorLabel.HIGHLIGHT.color());
	}

	private void drawHandShapeDrag(final Graphics g, final MouseButtonPressData press, final int x) {
		final HashSet2<CopiedPositionData<HandShape>> positions = selectionManager
				.getSelectedAccessor(PositionType.HAND_SHAPE).getSelectedSet()//
				.map(selection -> new CopiedPositionData<>((HandShape) selection.selectable));
		if (positions.isEmpty()) {
			return;
		}

		moveSelectedPositions(positions, press, x);

		positions.forEach(position -> makeDraggedHandShapeShape(position).draw(g));
	}

	private void drawNoneDrag(final Graphics g, final MouseButtonPressData press, final int x) {
	}

	private void drawVocalDrag(final Graphics g, final MouseButtonPressData press, final int x) {
		if (press.highlight.vocal == null) {
			return;
		}

		final int position = data.songChart.beatsMap.getPositionFromGridClosestTo(xToTime(x, data.time));
		final int dragX = timeToX(position, data.time);
		lineVertical(dragX, anchorY, lanesBottom, ColorLabel.HIGHLIGHT.color()).draw(g);
	}

	private final Map<PositionType, DragTypeDrawer> dragDrawers = new HashMap<>();

	{
		dragDrawers.put(PositionType.ANCHOR, this::drawAnchorDrag);
		dragDrawers.put(PositionType.BEAT, this::drawBeatDrag);
		dragDrawers.put(PositionType.EVENT_POINT, this::drawEventPointDrag);
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
		if (pressPosition == null || pressPosition.highlight.type != PositionType.GUITAR_NOTE) {
			return;
		}

		final int pressXTime = pressPosition.highlight.position();
		final int pressX = timeToX(pressXTime, data.time);
		final int pressY = pressPosition.position.y;

		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		final int currentX = timeToX(highlight.position(), data.time);
		line(new Position2D(pressX, pressY), new Position2D(currentX, y), ColorLabel.NOTE_ADD_LINE.color()).draw(g);

		final ArrayList2<PositionWithStringOrNoteId> dragPositions = highlightManager
				.getPositionsWithStrings(pressXTime, highlight.position(), pressY, y);
		for (final PositionWithStringOrNoteId position : dragPositions) {
			drawNoteHighlight(g, position.string, position.position(), data.currentStrings());
		}
	}

	private boolean tryToDrawDrag(final Graphics g) {
		final MouseButtonPressData leftPressPosition = mouseButtonPressReleaseHandler
				.getPressPosition(MouseButton.LEFT_BUTTON);
		if (leftPressPosition == null) {
			return false;
		}

		if (leftPressPosition.highlight.type == BEAT) {
			if (modeManager.editMode != EditMode.TEMPO_MAP) {
				return false;
			}

			final DragTypeDrawer drawer = dragDrawers.get(leftPressPosition.highlight.type);
			drawer.drawDrag(g, leftPressPosition, mouseHandler.getMouseX());
			return true;
		}

		if (abs(leftPressPosition.position.x - mouseHandler.getMouseX()) > 5) {
			final DragTypeDrawer drawer = dragDrawers.get(leftPressPosition.highlight.type);
			drawer.drawDrag(g, leftPressPosition, mouseHandler.getMouseX());
			return true;
		}

		return false;
	}

	public void draw(final Graphics g) {
		if (tryToDrawDrag(g)) {
			return;
		}

		final int x = mouseHandler.getMouseX();
		final int y = mouseHandler.getMouseY();
		final PositionWithIdAndType highlight = highlightManager.getHighlight(x, y);
		highlightDrawers.get(highlight.type).drawHighlight(g, highlight, x, y);

		drawNoteAdditionHighlight(g, x, y);
	}
}
