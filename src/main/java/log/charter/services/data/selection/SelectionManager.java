package log.charter.services.data.selection;

import static log.charter.util.CollectionUtils.closest;
import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.song.position.Position;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashMap2;

public class SelectionManager implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private CurrentSelectionEditor currentSelectionEditor;
	private ModeManager modeManager;

	private final Map<PositionType, SelectionList<?, ?, ?>> selectionLists = new HashMap2<>();

	@Override
	public void init() {
		for (final PositionType type : PositionType.values()) {
			final SelectionList<?, ?, ?> typeSelectionManager = new SelectionList<>(type);
			charterContext.initObject(typeSelectionManager);
			selectionLists.put(type, typeSelectionManager);
		}
	}

	private void clearSelectionsExcept(final PositionType typeNotToClear) {
		selectionLists.forEach((type, manager) -> {
			if (type != typeNotToClear) {
				manager.clear();
			}
		});
	}

	private static class PositionWithLink extends Position {
		public static List<PositionWithLink> fromPositionsWithIdAndType(final List<PositionWithIdAndType> positions) {
			final List<PositionWithLink> newPositions = new ArrayList<>(positions.size() * 2);

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

	private PositionWithIdAndType findExistingWithEnd(final int x, final List<PositionWithIdAndType> positions) {
		final List<PositionWithLink> positionsWithLinks = PositionWithLink.fromPositionsWithIdAndType(positions);
		final int position = xToTime(x, chartTimeHandler.time());
		final Integer id = closest(positionsWithLinks, new Position(position), IConstantPosition::compareTo,
				p -> p.position()).findId();
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

	private PositionWithIdAndType findClosestExistingPoint(final int x, final List<PositionWithIdAndType> positions) {
		final int position = xToTime(x, chartTimeHandler.time());
		final Integer id = closest(positions, new Position(position), IConstantPosition::compareTo, p -> p.position())
				.findId();
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
		final List<PositionWithIdAndType> positions = positionType.getPositionsWithIdsAndTypes(chartData);

		if (positionType == PositionType.HAND_SHAPE || positionType == PositionType.VOCAL) {
			return findExistingWithEnd(x, positions);
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

		final SelectionList<?, ?, ?> selectionList = selectionLists.get(clickData.pressHighlight.type);
		if (selectionList == null) {
			currentSelectionEditor.selectionChanged(true);
			return;
		}

		selectionList.addSelectablesWithModifiers(clickData.pressHighlight.id, ctrl, shift);
		currentSelectionEditor.selectionChanged(true);
	}

	public void clear() {
		clearSelectionsExcept(PositionType.NONE);
		currentSelectionEditor.selectionChanged(true);
	}

	@SuppressWarnings("unchecked")
	public <T extends IVirtualConstantPosition> ISelectionAccessor<T> accessor(final PositionType type) {
		final SelectionList<?, ?, ?> selectionList = selectionLists.get(type);
		if (selectionList == null) {
			return new NoneSelectionAccessor<>();
		}

		return (SelectionAccessor<T>) selectionList.getAccessor();
	}

	public List<Integer> getSelectedIds(final PositionType type) {
		return accessor(type).getSelectedIds(type);
	}

	public <T extends IVirtualConstantPosition> List<Selection<T>> getSelected(final PositionType type) {
		return this.<T>accessor(type).getSelected();
	}

	public <T extends IVirtualConstantPosition> List<T> getSelectedElements(final PositionType type) {
		return this.<T>accessor(type).getSelectedElements();
	}

	public List<Selection<Vocal>> getSelectedVocals() {
		return getSelected(PositionType.VOCAL);
	}

	@SuppressWarnings("unchecked")
	public <T extends IVirtualConstantPosition> ISelectionAccessor<T> selectedAccessor() {
		for (final SelectionList<?, ?, ?> selectionList : selectionLists.values()) {
			final ISelectionAccessor<T> accessor = (ISelectionAccessor<T>) selectionList.getAccessor();
			if (accessor.isSelected()) {
				return accessor;
			}
		}

		return new NoneSelectionAccessor<T>();
	}

	public void selectAllNotes() {
		if (modeManager.getMode() == EditMode.GUITAR) {
			selectionLists.get(PositionType.GUITAR_NOTE).addAll();
		} else if (modeManager.getMode() == EditMode.VOCALS) {
			selectionLists.get(PositionType.VOCAL).addAll();
		}

		currentSelectionEditor.selectionChanged(true);
	}

	public void addSelection(final PositionType type, final int id) {
		selectionLists.get(type).add(id);
		currentSelectionEditor.selectionChanged(true);
	}

	public void addSoundSelection(final int id) {
		addSelection(PositionType.GUITAR_NOTE, id);
	}

	public void addSoundSelection(final ArrayList2<Integer> ids) {
		selectionLists.get(PositionType.GUITAR_NOTE).add(ids);
		currentSelectionEditor.selectionChanged(true);
	}

	@SuppressWarnings("unchecked")
	public <C extends IVirtualConstantPosition> void addSelectionForPositions(final PositionType type,
			final Collection<C> positions) {
		((SelectionList<C, ?, ?>) selectionLists.get(type)).addPositions(positions);
		currentSelectionEditor.selectionChanged(type == PositionType.GUITAR_NOTE);
	}
}
