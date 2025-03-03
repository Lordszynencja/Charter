package log.charter.services.mouseAndKeyboard;

import static log.charter.util.CollectionUtils.contains;
import static log.charter.util.CollectionUtils.map;
import static log.charter.util.ScalingUtils.xToPosition;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.config.Zoom;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.time.Position;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPosition;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.VocalPane;
import log.charter.io.Logger;
import log.charter.services.ActionHandler;
import log.charter.services.data.BeatsService;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
	private ActionHandler actionHandler;
	private ArrangementFixer arrangementFixer;
	private BeatsService beatsService;
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
		Logger.debug("Mouse pressed, key: " + e.getButton() + ", position: (" + e.getX() + "," + e.getY() + ")");

		try {
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
		} catch (final Exception ex) {
			Logger.error("Exception on mouse pressed", ex);
		}
	}

	private void leftClickGuitar(final MouseButtonPressReleaseData clickData) {
		if (!clickData.isXDrag()) {
			selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
			return;
		}

		if (clickData.pressHighlight.type == PositionType.EVENT_POINT) {
			dragPositions(PositionType.EVENT_POINT, clickData, chartData.currentEventPoints());
		}
		if (clickData.pressHighlight.type == PositionType.TONE_CHANGE) {
			dragPositions(PositionType.TONE_CHANGE, clickData, chartData.currentToneChanges());
		}
		if (clickData.pressHighlight.type == PositionType.FHP) {
			dragPositions(PositionType.FHP, clickData, chartData.currentFHPs());
		}
		if (clickData.pressHighlight.type == PositionType.GUITAR_NOTE) {
			dragSounds(clickData, chartData.currentSounds());
		}
		if (clickData.pressHighlight.type == PositionType.HAND_SHAPE) {
			dragPositionsWithLength(PositionType.HAND_SHAPE, clickData, chartData.currentHandShapes());
		}
	}

	private void leftClickVocals(final MouseButtonPressReleaseData clickData, final boolean wasDoubleClick) {
		if (!clickData.isXDrag()) {
			selectionManager.click(clickData, keyboardHandler.ctrl(), keyboardHandler.shift());
		} else if (clickData.pressHighlight.type == PositionType.VOCAL) {
			dragPositionsWithLength(PositionType.VOCAL, clickData, chartData.currentVocals().vocals);
		}

		if (wasDoubleClick && clickData.pressHighlight.vocal != null) {
			new VocalPane(clickData.pressHighlight.id, clickData.pressHighlight.vocal, chartData, charterFrame,
					selectionManager, undoSystem);
		}
	}

	private void handleClick(final MouseButtonPressReleaseData clickData) {
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
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		Logger.debug("Mouse released, key: " + e.getButton() + ", position: (" + e.getX() + "," + e.getY() + ")");

		try {
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

			handleClick(clickData);

			mouseButtonPressReleaseHandler.remove(e);
			cancelAllActions();
			actionHandler.clearNumbers();
		} catch (final Exception ex) {
			Logger.error("Exception on mouse released", ex);
		}
	}

	private void dragTempo(final MouseButtonPressReleaseData clickData) {
		final double to = xToPosition(clickData.releasePosition.x, chartTimeHandler.time());
		beatsService.dragTempo(clickData.pressHighlight, to);
	}

	private void reselectDraggedPositions(final PositionType type,
			final List<? extends IVirtualConstantPosition> moved) {
		selectionManager.clear();
		selectionManager.addSelectionForPositions(type, moved);
	}

	private IVirtualConstantPosition findGridPositionClosestToX(final int x) {
		return chartData.beats().getPositionFromGridClosestTo(new Position(xToPosition(x, chartTimeHandler.time())));
	}

	private <T extends IVirtualPosition> void dragPositions(final PositionType type,
			final MouseButtonPressReleaseData clickData, final List<T> allPositions) {
		List<Selection<T>> selectedPositions = selectionManager.<T>accessor(type).getSelected();

		if (clickData.pressHighlight.existingPosition//
				&& !contains(selectedPositions, s -> s.id == clickData.pressHighlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(type, clickData.pressHighlight.id);
			selectedPositions = selectionManager.<T>accessor(type)//
					.getSelected();
		}
		if (selectedPositions.isEmpty()) {
			return;
		}

		final List<T> positions = map(selectedPositions, p -> p.selectable);
		undoSystem.addUndo();

		final IConstantFractionalPosition dragFrom = clickData.pressHighlight.toFraction(chartData.beats());
		final IConstantFractionalPosition dragTo = findGridPositionClosestToX(clickData.releasePosition.x)
				.toFraction(chartData.beats());

		chartData.beats().movePositions(positions, dragFrom.movementTo(dragTo));

		allPositions.sort(IVirtualConstantPosition.comparator(chartData.beats()));

		reselectDraggedPositions(type, positions);
	}

	private <T extends IVirtualPositionWithEnd> void dragPositionsWithLength(final PositionType type,
			final MouseButtonPressReleaseData clickData, final List<T> allPositions) {
		List<Selection<T>> selectedPositions = selectionManager.<T>accessor(clickData.pressHighlight.type)//
				.getSelected();

		if (clickData.pressHighlight.existingPosition//
				&& !contains(selectedPositions, s -> s.id == clickData.pressHighlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(clickData.pressHighlight.type, clickData.pressHighlight.id);
			selectedPositions = selectionManager.<T>accessor(clickData.pressHighlight.type)//
					.getSelected();
		}
		if (selectedPositions.isEmpty()) {
			return;
		}

		final List<T> positions = map(selectedPositions, p -> p.selectable);

		undoSystem.addUndo();

		final IConstantFractionalPosition dragFrom = clickData.pressHighlight.toFraction(chartData.beats());
		final IConstantFractionalPosition dragTo = findGridPositionClosestToX(clickData.releasePosition.x)
				.toFraction(chartData.beats());
		chartData.beats().movePositions(positions, dragFrom.movementTo(dragTo));

		allPositions.sort(IVirtualConstantPosition.comparator(chartData.beats()));

		arrangementFixer.fixLengths(allPositions);

		reselectDraggedPositions(type, positions);
	}

	private void dragSounds(final MouseButtonPressReleaseData clickData, final List<ChordOrNote> allPositions) {
		List<Selection<ChordOrNote>> selectedPositions = selectionManager
				.<ChordOrNote>accessor(clickData.pressHighlight.type).getSelected();

		if (clickData.pressHighlight.existingPosition//
				&& !contains(selectedPositions, s -> s.id == clickData.pressHighlight.id)) {
			selectionManager.clear();
			selectionManager.addSelection(clickData.pressHighlight.type, clickData.pressHighlight.id);
			selectedPositions = selectionManager.<ChordOrNote>accessor(clickData.pressHighlight.type)//
					.getSelected();
		}
		if (selectedPositions.isEmpty()) {
			return;
		}

		final List<ChordOrNote> positions = map(selectedPositions, p -> (ChordOrNote) p.selectable);
		undoSystem.addUndo();

		final IConstantFractionalPosition dragFrom = clickData.pressHighlight.toFraction(chartData.beats());
		final IConstantFractionalPosition dragTo = findGridPositionClosestToX(clickData.releasePosition.x)
				.toFraction(chartData.beats());

		chartData.beats().moveSounds(positions, dragFrom.movementTo(dragTo));

		allPositions.sort(IConstantFractionalPosition::compareTo);

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

	public PositionType getMouseHoverPositionType() {
		return PositionType.fromY(mouseY, modeManager.getMode());
	}

	public void cancelAllActions() {
		mouseButtonPressReleaseHandler.clear();
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		Logger.debug("Mouse wheel moved, rotation: " + e.getWheelRotation());

		if (chartData.isEmpty) {
			return;
		}

		try {
			final int change = -e.getWheelRotation();
			if (keyboardHandler.ctrl()) {
				final int zoomChange = change * (keyboardHandler.shift() ? 8 : 1);
				Zoom.addZoom(zoomChange);
				return;
			}

			if (!selectionManager.selectedAccessor().isSelected()) {
				return;
			}

			modeManager.getHandler().changeLength(change);
		} catch (final Exception ex) {
			Logger.error("Exception on mouse wheel moved", ex);
		}
	}

}
