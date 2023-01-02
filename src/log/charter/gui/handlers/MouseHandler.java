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

import log.charter.data.ChartData;
import log.charter.data.config.Zoom;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.song.ArrangementChart;
import log.charter.song.Level;
import log.charter.song.notes.IPosition;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
	private ChartData data;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private boolean pressCancelsRelease = false;
	private boolean releaseCancelled = false;
	private int mouseX = -1;
	private int mouseY = -1;

	public void init(final AudioHandler audioHandler, final ChartData data, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager, final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.data = data;
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

		if (e.getButton() == MouseEvent.BUTTON1) {
			selectionManager.click(e.getX(), e.getY(), keyboardHandler.ctrl(), keyboardHandler.shift());
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
		mouseMoved(e);

		if (data.isEmpty) {
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
			if (!clickData.isDrag()) {
				break;
			}
			if (clickData.pressHighlight.beat != null) {
				dragTempo(clickData);
			}
			if (clickData.pressHighlight.chordOrNote != null) {
				// TODO drag and snap notes?
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
		while (id < positions.size() && positions.get(id).position() <= rightPosition) {
			right.add(positions.get(id));
			id++;
		}
	}

	private void getAllLeftRightPositions(final int leftPosition, final int middlePosition, final int rightPosition,
			final List<IPosition> left, final List<IPosition> right) {
		splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, data.songChart.beatsMap.beats);
		splitToLeftRight(leftPosition, middlePosition, rightPosition, left, right, data.songChart.vocals.vocals);

		for (final ArrangementChart arrangement : data.songChart.arrangements) {
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

	private void dragTempo(final MouseButtonPressReleaseData clickData) {
		if (modeManager.editMode != EditMode.TEMPO_MAP) {
			return;
		}

		undoSystem.addUndo();

		clickData.pressHighlight.beat.anchor = true;

		final int leftId = data.songChart.beatsMap.findPreviousAnchoredBeat(clickData.pressHighlight.id);
		// TODO add fix if right is not anchored
		final int rightId = data.songChart.beatsMap.findNextAnchoredBeat(clickData.pressHighlight.id);

		final int leftPosition = data.songChart.beatsMap.beats.get(leftId).position();
		final int middlePositionBefore = clickData.pressHighlight.position();
		final int middlePositionAfter = max(0,
				min(data.music.msLength(), xToTime(clickData.releasePosition.x, data.time)));
		final int rightPosition = data.songChart.beatsMap.beats.get(rightId).position();

		final List<IPosition> left = new ArrayList<>();
		final List<IPosition> right = new ArrayList<>();

		getAllLeftRightPositions(leftPosition, middlePositionBefore, rightPosition, left, right);
		movePositionsBasedOnBeatsChange(leftPosition, middlePositionBefore, leftPosition, middlePositionAfter, left);
		movePositionsBasedOnBeatsChange(middlePositionBefore, rightPosition, middlePositionAfter, rightPosition, right);

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
		final int change = -e.getWheelRotation();
		if (keyboardHandler.ctrl()) {
			final int zoomChange = change * (keyboardHandler.shift() ? 1 : 8);
			Zoom.addZoom(zoomChange);
			return;
		}

		modeManager.getHandler().changeLength(change);
	}

}
