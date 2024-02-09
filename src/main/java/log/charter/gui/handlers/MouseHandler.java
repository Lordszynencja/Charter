package log.charter.gui.handlers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static log.charter.util.ScalingUtils.xToTime;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Zoom;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.gui.panes.VocalPane;
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.song.Level;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.IPositionWithLength;
import log.charter.util.CollectionUtils.ArrayList2;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
	private ArrangementFixer arrangementFixer;
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private boolean pressCancelsRelease = false;
	private boolean releaseCancelled = false;
	private int mouseX = -1;
	private int mouseY = -1;
	private long lastLeftClickTime = 0;
	private Integer lastClickId = null;

	public void init(final ArrangementFixer arrangementFixer, final ChartData data, final CharterFrame frame,
			final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.arrangementFixer = arrangementFixer;
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	/*
	 * invoked only when it wasn't dragged
	 */
	@Override
	public void mouseClicked(final MouseEvent e) {
		mouseMoved(e);

		if (releaseCancelled) {
			pressCancelsRelease = false;
			return;
		}
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getComponent() != null) {
			e.getComponent().requestFocus();
		}

		mouseMoved(e);

		if (data.isEmpty) {
			return;
		}

		keyboardHandler.clearFretNumber();
		cancelAllActions();
		if (pressCancelsRelease) {
			releaseCancelled = true;
			return;
		} else {
			pressCancelsRelease = true;
			releaseCancelled = false;
		}

		mouseButtonPressReleaseHandler.press(e);
	}

	private void leftClickGuitar(final MouseButtonPressReleaseData clickData) {
		if (!clickData.isXDrag()) {
			selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
			return;
		}

		if (clickData.pressHighlight.type == PositionType.EVENT_POINT) {
			dragPositions(PositionType.EVENT_POINT, clickData, data.getCurrentArrangement().eventPoints);
		}
		if (clickData.pressHighlight.type == PositionType.TONE_CHANGE) {
			dragPositions(PositionType.TONE_CHANGE, clickData, data.getCurrentArrangement().toneChanges);
		}
		if (clickData.pressHighlight.type == PositionType.ANCHOR) {
			dragPositions(PositionType.ANCHOR, clickData, data.getCurrentArrangementLevel().anchors);
		}
		if (clickData.pressHighlight.type == PositionType.GUITAR_NOTE) {
			dragNotes(clickData, data.getCurrentArrangementLevel().chordsAndNotes);
		}
		if (clickData.pressHighlight.type == PositionType.HAND_SHAPE) {
			dragPositionsWithLength(PositionType.HAND_SHAPE, clickData, data.getCurrentArrangementLevel().handShapes);
		}
	}

	private void leftClickVocals(final MouseButtonPressReleaseData clickData, final boolean wasDoubleClick) {
		if (!clickData.isXDrag()) {
			selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
		} else if (clickData.pressHighlight.type == PositionType.VOCAL) {
			dragPositionsWithLength(PositionType.VOCAL, clickData, data.songChart.vocals.vocals);
		}

		if (wasDoubleClick && clickData.pressHighlight.vocal != null) {
			new VocalPane(clickData.pressHighlight.id, clickData.pressHighlight.vocal, data, frame, selectionManager,
					undoSystem);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		mouseMoved(e);

		if (data.isEmpty) {
			return;
		}

		pressCancelsRelease = false;
		if (releaseCancelled) {
			return;
		}

		final MouseButtonPressReleaseData clickData = mouseButtonPressReleaseHandler.release(e);
		if (clickData == null) {
			return;
		}

		switch (clickData.button) {
			case LEFT_BUTTON:
				final boolean wasLeftDoubleClick = lastClickId != null
						&& lastClickId.equals(clickData.pressHighlight.id) //
						&& System.currentTimeMillis() - lastLeftClickTime < 300;
				lastLeftClickTime = System.currentTimeMillis();
				lastClickId = clickData.pressHighlight.id;

				switch (modeManager.getMode()) {
					case GUITAR:
						leftClickGuitar(clickData);
						break;
					case TEMPO_MAP:
						dragTempo(clickData);
						break;
					case VOCALS:
						leftClickVocals(clickData, wasLeftDoubleClick);
						break;
					default:
						break;
				}
				break;
			case RIGHT_BUTTON:
				modeManager.getHandler().rightClick(clickData);
				break;
			default:
				break;
		}

		mouseButtonPressReleaseHandler.remove(e);
		cancelAllActions();
	}

	private void splitToLeftRight(final int leftPosition, final int middlePosition, final int rightPosition,
			final List<IPosition> left, final List<IPosition> right, final List<? extends IPosition> positions) {
		int id = 0;
		while (id < positions.size() && positions.get(id).position() < leftPosition) {
			id++;
		}

		while (id < positions.size() && positions.get(id).position() < middlePosition) {
			left.add(positions.get(id));
			id++;
		}
		while (id < positions.size() && positions.get(id).position() < rightPosition) {
			right.add(positions.get(id));
			id++;
		}
	}

	private void getAllLeftRightPositions(final int leftPosition, final int middlePosition, final int rightPosition,
			final List<IPosition> left, final List<IPosition> right) {
		splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, data.songChart.vocals.vocals);

		for (final ArrangementChart arrangement : data.songChart.arrangements) {
			splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, arrangement.toneChanges);
			for (final Level level : arrangement.levels.values()) {
				splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, level.anchors);
				splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, level.chordsAndNotes);
				splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, level.handShapes);
			}
		}
	}

	private void movePositionsBasedOnBeatsChange(final int positionFromBefore, final int positionToBefore,
			final int positionFromAfter, final int positionToAfter, final List<IPosition> positionsToChange) {
		final int lengthBefore = positionToBefore - positionFromBefore;
		final int lengthAfter = positionToAfter - positionFromAfter;
		for (final IPosition position : positionsToChange) {
			final double offsetByOldFromPosition = position.position() - positionFromBefore;
			final double dividedByOldLength = offsetByOldFromPosition / lengthBefore;
			final double multipliedByNewLength = dividedByOldLength * lengthAfter;
			final int newPosition = (int) (multipliedByNewLength + positionFromAfter);
			position.position(newPosition);
		}
	}

	private void straightenBeats(final int from, final int to) {
		final int positionFrom = data.songChart.beatsMap.beats.get(from).position();
		final int positionTo = data.songChart.beatsMap.beats.get(to).position();
		final int size = to - from;

		for (int i = 1; i < size; i++) {
			final int beatId = from + i;
			final int beatPosition = (positionFrom * (size - i) + positionTo * i) / size;
			data.songChart.beatsMap.beats.get(beatId).position(beatPosition);
		}
	}

	private void dragTempo(final MouseButtonPressReleaseData clickData) {
		if (modeManager.getMode() != EditMode.TEMPO_MAP || !clickData.pressHighlight.existingPosition) {
			return;
		}

		undoSystem.addUndo();

		if (clickData.pressHighlight.id != null && clickData.pressHighlight.id == 0 || keyboardHandler.alt()) {
			final int positionBefore = clickData.pressHighlight.position();
			final int positionAfter = max(0,
					min(data.music.msLength(), xToTime(clickData.releasePosition.x, data.time)));
			data.songChart.moveEverything(positionAfter - positionBefore);
			return;
		}

		clickData.pressHighlight.beat.anchor = true;

		final ArrayList2<Beat> beats = data.songChart.beatsMap.beats;

		final int leftId = data.songChart.beatsMap.findPreviousAnchoredBeat(clickData.pressHighlight.id);
		final int middleId = clickData.pressHighlight.id;
		final Integer rightId = data.songChart.beatsMap.findNextAnchoredBeat(clickData.pressHighlight.id);

		final int leftPosition = beats.get(leftId).position();
		final int minNewPosition = leftPosition + (middleId - leftId) * 10;
		final int middlePositionBefore = clickData.pressHighlight.position();
		int middlePositionAfter = max(minNewPosition,
				min(data.music.msLength(), xToTime(clickData.releasePosition.x, data.time)));
		final int rightPositionBefore;
		final int rightPositionAfter;

		if (rightId != null) {
			rightPositionBefore = beats.get(rightId).position();
			rightPositionAfter = rightPositionBefore;
			final int maxNewPosition = rightPositionAfter - (rightId - middleId) * 10;
			middlePositionAfter = min(maxNewPosition, middlePositionAfter);
		} else if (beats.size() > middleId + 1) {
			rightPositionBefore = beats.getLast().position();
			final int beatLength = middleId == leftId ? 500
					: (middlePositionAfter - leftPosition) / (middleId - leftId);
			rightPositionAfter = middlePositionAfter + (beats.size() - middleId - 1) * beatLength;
		} else {
			rightPositionBefore = middlePositionBefore;
			rightPositionAfter = middlePositionAfter;
		}

		final List<IPosition> left = new ArrayList<>();
		final List<IPosition> right = new ArrayList<>();

		getAllLeftRightPositions(leftPosition, middlePositionBefore, rightPositionBefore, left, right);
		if (!left.isEmpty()) {
			movePositionsBasedOnBeatsChange(leftPosition, middlePositionBefore, leftPosition, middlePositionAfter,
					left);
		}
		if (!right.isEmpty()) {
			movePositionsBasedOnBeatsChange(middlePositionBefore, rightPositionBefore, middlePositionAfter,
					rightPositionAfter, right);
		}

		clickData.pressHighlight.beat.position(middlePositionAfter);

		straightenBeats(leftId, middleId);

		if (rightId != null) {
			straightenBeats(middleId, rightId);
		} else {
			final int basePosition = beats.get(middleId).position();
			final int distance = basePosition - beats.get(middleId - 1).position();
			for (int i = middleId + 1; i < beats.size(); i++) {
				beats.get(i).position(basePosition + (i - middleId) * distance);
			}
		}

		data.songChart.beatsMap.makeBeatsUntilSongEnd();
	}

	private void reselectDraggedPositions(final PositionType type, final List<? extends IPosition> moved) {
		final Set<Integer> movedPositions = moved.stream().map(IPosition::position).collect(Collectors.toSet());
		selectionManager.clear();
		selectionManager.addSelectionForPositions(type, movedPositions);
	}

	private void dragPositions(final PositionType type, final MouseButtonPressReleaseData clickData,
			final List<? extends IPosition> allPositions) {
		ArrayList2<Selection<IPosition>> selectedPositions = selectionManager
				.getSelectedAccessor(clickData.pressHighlight.type).getSortedSelected();

		if (clickData.pressHighlight.existingPosition//
				&& !selectedPositions.contains(selection -> selection.id == clickData.pressHighlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(clickData.pressHighlight.type, clickData.pressHighlight.id);
			selectedPositions = selectionManager.getSelectedAccessor(clickData.pressHighlight.type)//
					.getSortedSelected();
		}
		if (selectedPositions.isEmpty()) {
			return;
		}

		final ArrayList2<IPosition> positions = selectedPositions.map(p -> p.selectable);
		undoSystem.addUndo();

		final int dragFrom = clickData.pressHighlight.position();
		final int dragTo = data.songChart.beatsMap
				.getPositionFromGridClosestTo(xToTime(clickData.releasePosition.x, data.time));
		data.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);

		allPositions.sort(null);

		reselectDraggedPositions(type, positions);
	}

	private void dragPositionsWithLength(final PositionType type, final MouseButtonPressReleaseData clickData,
			final ArrayList2<? extends IPositionWithLength> allPositions) {
		ArrayList2<Selection<IPosition>> selectedPositions = selectionManager
				.getSelectedAccessor(clickData.pressHighlight.type)//
				.getSortedSelected();

		if (clickData.pressHighlight.existingPosition//
				&& !selectedPositions.contains(selection -> selection.id == clickData.pressHighlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(clickData.pressHighlight.type, clickData.pressHighlight.id);
			selectedPositions = selectionManager.getSelectedAccessor(clickData.pressHighlight.type)//
					.getSortedSelected();
		}
		if (selectedPositions.isEmpty()) {
			return;
		}

		final ArrayList2<IPosition> positions = selectedPositions.map(p -> p.selectable);

		undoSystem.addUndo();

		final int dragFrom = clickData.pressHighlight.position();
		final int dragTo = data.songChart.beatsMap
				.getPositionFromGridClosestTo(xToTime(clickData.releasePosition.x, data.time));
		data.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);

		allPositions.sort(null);

		arrangementFixer.fixLengths(allPositions);

		reselectDraggedPositions(type, positions);
	}

	private void dragNotes(final MouseButtonPressReleaseData clickData, final ArrayList2<ChordOrNote> allPositions) {
		ArrayList2<Selection<IPosition>> selectedPositions = selectionManager
				.getSelectedAccessor(clickData.pressHighlight.type).getSortedSelected();

		if (clickData.pressHighlight.existingPosition//
				&& !selectedPositions.contains(selection -> selection.id == clickData.pressHighlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(clickData.pressHighlight.type, clickData.pressHighlight.id);
			selectedPositions = selectionManager.getSelectedAccessor(clickData.pressHighlight.type)//
					.getSortedSelected();
		}
		if (selectedPositions.isEmpty()) {
			return;
		}

		final ArrayList2<ChordOrNote> positions = selectedPositions.map(p -> (ChordOrNote) p.selectable);
		undoSystem.addUndo();

		final int dragFrom = clickData.pressHighlight.position();
		final int dragTo = data.songChart.beatsMap
				.getPositionFromGridClosestTo(xToTime(clickData.releasePosition.x, data.time));
		data.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);

		allPositions.sort(null);

		arrangementFixer.fixNoteLengths(allPositions);

		reselectDraggedPositions(PositionType.GUITAR_NOTE, positions);
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	public void cancelAllActions() {
		mouseButtonPressReleaseHandler.clear();
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		if (data.isEmpty) {
			return;
		}

		final int change = -e.getWheelRotation();
		if (keyboardHandler.ctrl()) {
			final int zoomChange = change * (keyboardHandler.shift() ? 8 : 1);
			Zoom.addZoom(zoomChange);
			return;
		}

		if (!selectionManager.getCurrentlySelectedAccessor().isSelected()) {
			return;
		}

		modeManager.getHandler().changeLength(change);
	}

}
