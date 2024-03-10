package log.charter.services.mouseAndKeyboard;

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

import log.charter.data.ChartData;
import log.charter.data.config.Zoom;
import log.charter.data.song.Arrangement;
import log.charter.data.song.Beat;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.IPosition;
import log.charter.data.song.notes.IPositionWithLength;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.util.CollectionUtils.ArrayList2;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
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

		if (chartData.isEmpty) {
			return;
		}

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
			dragPositions(PositionType.EVENT_POINT, clickData, chartData.getCurrentArrangement().eventPoints);
		}
		if (clickData.pressHighlight.type == PositionType.TONE_CHANGE) {
			dragPositions(PositionType.TONE_CHANGE, clickData, chartData.getCurrentArrangement().toneChanges);
		}
		if (clickData.pressHighlight.type == PositionType.ANCHOR) {
			dragPositions(PositionType.ANCHOR, clickData, chartData.getCurrentArrangementLevel().anchors);
		}
		if (clickData.pressHighlight.type == PositionType.GUITAR_NOTE) {
			dragNotes(clickData, chartData.getCurrentArrangementLevel().sounds);
		}
		if (clickData.pressHighlight.type == PositionType.HAND_SHAPE) {
			dragPositionsWithLength(PositionType.HAND_SHAPE, clickData,
					chartData.getCurrentArrangementLevel().handShapes);
		}
	}

	private void leftClickVocals(final MouseButtonPressReleaseData clickData, final boolean wasDoubleClick) {
		if (!clickData.isXDrag()) {
			selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
		} else if (clickData.pressHighlight.type == PositionType.VOCAL) {
			dragPositionsWithLength(PositionType.VOCAL, clickData, chartData.songChart.vocals.vocals);
		}

		if (wasDoubleClick && clickData.pressHighlight.vocal != null) {
			new VocalPane(clickData.pressHighlight.id, clickData.pressHighlight.vocal, chartData, charterFrame,
					selectionManager, undoSystem);
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		mouseMoved(e);

		if (chartData.isEmpty) {
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
		splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, chartData.songChart.vocals.vocals);

		for (final Arrangement arrangement : chartData.songChart.arrangements) {
			splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, arrangement.toneChanges);
			for (final Level level : arrangement.levels) {
				splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, level.anchors);
				splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, level.sounds);
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
		final int positionFrom = chartData.songChart.beatsMap.beats.get(from).position();
		final int positionTo = chartData.songChart.beatsMap.beats.get(to).position();
		final int size = to - from;

		for (int i = 1; i < size; i++) {
			final int beatId = from + i;
			final int beatPosition = (positionFrom * (size - i) + positionTo * i) / size;
			chartData.songChart.beatsMap.beats.get(beatId).position(beatPosition);
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
					min(chartTimeHandler.maxTime(), xToTime(clickData.releasePosition.x, chartTimeHandler.time())));
			chartData.songChart.moveEverything(chartTimeHandler.maxTime(), positionAfter - positionBefore);
			return;
		}

		clickData.pressHighlight.beat.anchor = true;

		final ArrayList2<Beat> beats = chartData.songChart.beatsMap.beats;

		final int leftId = chartData.songChart.beatsMap.findPreviousAnchoredBeat(clickData.pressHighlight.id);
		final int middleId = clickData.pressHighlight.id;
		final Integer rightId = chartData.songChart.beatsMap.findNextAnchoredBeat(clickData.pressHighlight.id);

		final int leftPosition = beats.get(leftId).position();
		final int minNewPosition = leftPosition + (middleId - leftId) * 10;
		final int middlePositionBefore = clickData.pressHighlight.position();
		int middlePositionAfter = max(minNewPosition,
				min(chartTimeHandler.maxTime(), xToTime(clickData.releasePosition.x, chartTimeHandler.time())));
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

		chartData.songChart.beatsMap.makeBeatsUntilSongEnd(chartTimeHandler.maxTime());
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
		final int dragTo = chartData.songChart.beatsMap
				.getPositionFromGridClosestTo(xToTime(clickData.releasePosition.x, chartTimeHandler.time()));
		chartData.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);

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
		final int dragTo = chartData.songChart.beatsMap
				.getPositionFromGridClosestTo(xToTime(clickData.releasePosition.x, chartTimeHandler.time()));
		chartData.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);

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
		final int dragTo = chartData.songChart.beatsMap
				.getPositionFromGridClosestTo(xToTime(clickData.releasePosition.x, chartTimeHandler.time()));
		chartData.songChart.beatsMap.movePositions(dragFrom, dragTo, positions);

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
		if (chartData.isEmpty) {
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
