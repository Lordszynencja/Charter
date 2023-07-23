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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import log.charter.song.ArrangementChart;
import log.charter.song.Beat;
import log.charter.song.Level;
import log.charter.song.notes.IPosition;
import log.charter.util.CollectionUtils.ArrayList2;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
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

	public void init(final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
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
			switch (modeManager.editMode) {
			case GUITAR:
				if (!clickData.isXDrag()) {
					selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
					break;
				}
				if (clickData.pressHighlight.chordOrNote != null) {
					dragNotes(clickData);
				}
				break;
			case TEMPO_MAP:
				if (modeManager.editMode == EditMode.TEMPO_MAP) {
					dragTempo(clickData);
				} else {
					selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
				}
				break;
			case VOCALS:
				if (!clickData.isXDrag()) {
					selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
					break;
				}
				if (clickData.pressHighlight.vocal != null) {
					dragVocals(clickData);
				}
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
		if (modeManager.editMode != EditMode.TEMPO_MAP || !clickData.pressHighlight.existingPosition) {
			return;
		}

		undoSystem.addUndo();

		if (clickData.pressHighlight.id != null && clickData.pressHighlight.id == 0) {
			final int positionBefore = clickData.pressHighlight.position();
			final int positionAfter = max(0,
					min(data.music.msLength(), xToTime(clickData.releasePosition.x, data.time)));
			moveEverything(positionAfter - positionBefore);
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
			// TODO change positions of beats on the right to middleId
			final int basePosition = beats.get(middleId).position();
			final int distance = basePosition - beats.get(middleId - 1).position();
			for (int i = middleId + 1; i < beats.size(); i++) {
				beats.get(i).position(basePosition + (i - middleId) * distance);
			}
		}

		data.songChart.beatsMap.makeBeatsUntilSongEnd();
	}

	private void moveEverything(final int positionDifference) {
		final List<IPosition> positionsToMove = new LinkedList<>();
		positionsToMove.addAll(data.songChart.beatsMap.beats);
		for (final ArrangementChart arrangement : data.songChart.arrangements) {
			positionsToMove.addAll(arrangement.toneChanges);
			for (final Level level : arrangement.levels.values()) {
				positionsToMove.addAll(level.anchors);
				positionsToMove.addAll(level.chordsAndNotes);
				positionsToMove.addAll(level.handShapes);
			}
		}
		positionsToMove.addAll(data.songChart.vocals.vocals);

		final int maxPosition = data.music.msLength();
		for (final IPosition positionToMove : positionsToMove) {
			positionToMove.position(min(maxPosition, max(0, positionToMove.position() + positionDifference)));
		}

		data.songChart.beatsMap.makeBeatsUntilSongEnd();
	}

	private void dragPositions(final int start, final int end, final ArrayList2<Selection<IPosition>> positions) {
		if (positions.isEmpty()) {
			return;
		}

		undoSystem.addUndo();

		final double startInBeats = data.songChart.beatsMap.getPositionInBeats(start);
		final double endInBeats = data.songChart.beatsMap.getPositionInBeats(end);
		final double add = endInBeats - startInBeats;
		for (final Selection<IPosition> selection : positions) {
			final IPosition position = selection.selectable;
			final double positionInBeats = data.songChart.beatsMap.getPositionInBeats(position.position());
			final int newPosition = data.songChart.beatsMap.getPositionForPositionInBeats(positionInBeats + add);
			position.position(newPosition);
		}
	}

	private void sortAndReselectDraggedPositions(final ArrayList2<Selection<IPosition>> moved,
			final List<? extends IPosition> allPositions) {
		allPositions.sort(null);

		final Set<Integer> movedPositions = moved.stream().map(p -> p.selectable.position())
				.collect(Collectors.toSet());
		selectionManager.clear();
		for (int i = 0; i < allPositions.size(); i++) {
			final IPosition position = allPositions.get(i);
			if (movedPositions.contains(position.position())) {
				selectionManager.addSoundSelection(i);
			}
		}
	}

	private void dragNotes(final MouseButtonPressReleaseData clickData) {
		final int dragStartPosition = clickData.pressHighlight.chordOrNote.position();
		final int dragEndPosition = clickData.releaseHighlight.position();
		final ArrayList2<Selection<IPosition>> positions = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE).getSortedSelected();
		dragPositions(dragStartPosition, dragEndPosition, positions);

		sortAndReselectDraggedPositions(positions, data.getCurrentArrangementLevel().chordsAndNotes);

		frame.selectionChanged(false);
	}

	private void dragVocals(final MouseButtonPressReleaseData clickData) {
		final int dragStartPosition = clickData.pressHighlight.vocal.position();
		final int dragEndPosition = clickData.releaseHighlight.position();
		final ArrayList2<Selection<IPosition>> positions = selectionManager.getSelectedAccessor(PositionType.VOCAL)
				.getSortedSelected();
		dragPositions(dragStartPosition, dragEndPosition, positions);

		sortAndReselectDraggedPositions(positions, data.songChart.vocals.vocals);

		frame.selectionChanged(false);
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
			final int zoomChange = change * (keyboardHandler.shift() ? 1 : 8);
			Zoom.addZoom(zoomChange);
			return;
		}

		modeManager.getHandler().changeLength(change);
	}

}
