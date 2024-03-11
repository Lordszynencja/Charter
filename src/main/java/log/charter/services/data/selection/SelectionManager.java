package log.charter.services.data.selection;

import static log.charter.data.song.position.IConstantPosition.findClosestId;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.Map;
import java.util.Set;

import log.charter.data.ChartData;
import log.charter.data.song.position.IPosition;
import log.charter.data.song.position.Position;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashMap2;

public class SelectionManager implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private CurrentSelectionEditor currentSelectionEditor;
	private ModeManager modeManager;
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;

	private final Map<PositionType, TypeSelectionManager<?>> typeSelectionManagers = new HashMap2<>();

	@Override
	public void init() {
		for (final PositionType type : PositionType.values()) {
			final TypeSelectionManager<?> typeSelectionManager = new TypeSelectionManager<>(type, chartData,
					mouseButtonPressReleaseHandler);
			charterContext.initObject(typeSelectionManager);
			typeSelectionManagers.put(type, typeSelectionManager);
		}
	}

	private void clearSelectionsExcept(final PositionType typeNotToClear) {
		typeSelectionManagers.forEach((type, manager) -> {
			if (type != typeNotToClear) {
				manager.clear();
			}
		});
	}

	private static class PositionWithLink extends Position {
		public static ArrayList2<PositionWithLink> fromPositionsWithIdAndType(
				final ArrayList2<PositionWithIdAndType> positions) {
			final ArrayList2<PositionWithLink> newPositions = new ArrayList2<>(positions.size() * 2);

			for (final PositionWithIdAndType position : positions) {
				newPositions.add(new PositionWithLink(position.position(), position));
				newPositions.add(new PositionWithLink(position.endPosition, position));
			}

			return newPositions;
		}

		public final PositionWithIdAndType link;

		public PositionWithLink(final int position, final PositionWithIdAndType link) {
			super(position);
			this.link = link;
		}
	}

	private PositionWithIdAndType findExistingLong(final int x, final ArrayList2<PositionWithIdAndType> positions) {
		final ArrayList2<PositionWithLink> positionsWithLinks = PositionWithLink.fromPositionsWithIdAndType(positions);
		final int position = xToTime(x, chartTimeHandler.time());
		final Integer id = findClosestId(positionsWithLinks, position);
		if (id == null) {
			return null;
		}

		final PositionWithIdAndType closest = positionsWithLinks.get(id).link;
		if (x - timeToX(closest.position(), chartTimeHandler.time()) < -20
				|| x - timeToX(closest.endPosition, chartTimeHandler.time()) > 20) {
			return null;
		}

		return closest;
	}

	private PositionWithIdAndType findClosestExistingPoint(final int x,
			final ArrayList2<PositionWithIdAndType> positions) {
		final int position = xToTime(x, chartTimeHandler.time());
		final Integer id = findClosestId(positions, position);
		if (id == null) {
			return null;
		}

		final PositionWithIdAndType closest = positions.get(id);
		if (x - timeToX(closest.position(), chartTimeHandler.time()) < -20
				|| x - timeToX(closest.position(), chartTimeHandler.time()) > 20) {
			return null;
		}

		return closest;
	}

	public PositionWithIdAndType findExistingPosition(final int x, final int y) {
		final PositionType positionType = PositionType.fromY(y, modeManager.getMode());
		final ArrayList2<PositionWithIdAndType> positions = positionType.getPositionsWithIdsAndTypes(chartData);

		if (positionType == PositionType.HAND_SHAPE || positionType == PositionType.VOCAL) {
			return findExistingLong(x, positions);
		}

		return findClosestExistingPoint(x, positions);
	}

	public void click(final MouseButtonPressReleaseData clickData, final boolean ctrl, final boolean shift) {
		if (chartData.isEmpty) {
			return;
		}

		if (!clickData.pressHighlight.existingPosition) {
			if (!ctrl) {
				clearSelectionsExcept(PositionType.NONE);
			}

			currentSelectionEditor.selectionChanged(true);
			return;
		}

		clearSelectionsExcept(clickData.pressHighlight.type);

		final TypeSelectionManager<?> manager = typeSelectionManagers.get(clickData.pressHighlight.type);
		if (manager == null) {
			currentSelectionEditor.selectionChanged(true);
			return;
		}

		manager.addSelection(clickData.pressHighlight, clickData.pressPosition.x, clickData.pressPosition.y, ctrl,
				shift);
		currentSelectionEditor.selectionChanged(true);
	}

	public void clear() {
		clearSelectionsExcept(PositionType.NONE);
		currentSelectionEditor.selectionChanged(true);
	}

	public void refresh(final PositionType type) {
		typeSelectionManagers.get(type).refresh();
	}

	@SuppressWarnings("unchecked")
	public <T extends IPosition> SelectionAccessor<T> getSelectedAccessor(final PositionType type) {
		final TypeSelectionManager<?> typeSelectionManager = typeSelectionManagers.get(type);
		if (typeSelectionManager == null) {
			return new SelectionAccessor<>(PositionType.NONE, () -> new ArrayList2<>());
		}

		return (SelectionAccessor<T>) typeSelectionManager.getAccessor();
	}

	@SuppressWarnings("unchecked")
	public <T extends IPosition> SelectionAccessor<T> getCurrentlySelectedAccessor() {
		for (final TypeSelectionManager<?> typeSelectionManager : typeSelectionManagers.values()) {
			final SelectionAccessor<T> accessor = (SelectionAccessor<T>) typeSelectionManager.getAccessor();
			if (accessor.isSelected()) {
				return accessor;
			}
		}

		return (SelectionAccessor<T>) typeSelectionManagers.get(PositionType.NONE).getAccessor();
	}

	public void selectAllNotes() {
		if (modeManager.getMode() == EditMode.GUITAR) {
			typeSelectionManagers.get(PositionType.GUITAR_NOTE).addAll();
		} else if (modeManager.getMode() == EditMode.VOCALS) {
			typeSelectionManagers.get(PositionType.VOCAL).addAll();
		}

		currentSelectionEditor.selectionChanged(true);
	}

	public void addSelection(final PositionType type, final int id) {
		typeSelectionManagers.get(type).add(id);
		currentSelectionEditor.selectionChanged(true);
	}

	public void addSoundSelection(final int id) {
		addSelection(PositionType.GUITAR_NOTE, id);
	}

	public void addSoundSelection(final ArrayList2<Integer> ids) {
		typeSelectionManagers.get(PositionType.GUITAR_NOTE).add(ids);
		currentSelectionEditor.selectionChanged(true);
	}

	public void addSelectionForPositions(final PositionType type, final Set<Integer> positions) {
		typeSelectionManagers.get(type).addPositions(positions);
		currentSelectionEditor.selectionChanged(type == PositionType.GUITAR_NOTE);
	}

}
