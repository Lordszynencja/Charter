package log.charter.services.data.selection;

import static log.charter.data.config.Config.selectNotesByTails;
import static log.charter.util.CollectionUtils.closest;
import static log.charter.util.ScalingUtils.xToPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.position.time.ConstantPosition;
import log.charter.data.song.position.time.IConstantPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressReleaseData;
import log.charter.services.mouseAndKeyboard.MouseHandler;
import log.charter.util.collections.HashMap2;

public class SelectionManager implements Initiable {
	private CharterContext charterContext;
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private CurrentSelectionEditor currentSelectionEditor;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;

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

	private class PositionWithLink implements IConstantPosition {
		private final double position;
		public final PositionWithIdAndType link;

		public PositionWithLink(final double position, final PositionWithIdAndType link) {
			this.position = position;
			this.link = link;
		}

		@Override
		public double position() {
			return position;
		}
	}

	public List<PositionWithLink> generateLinks(final List<PositionWithIdAndType> positions) {
		final List<PositionWithLink> newPositions = new ArrayList<>(positions.size());

		final ImmutableBeatsMap beats = chartData.beats();
		for (final PositionWithIdAndType position : positions) {
			newPositions.add(new PositionWithLink(position.toPosition(beats).position(), position));
		}

		return newPositions;
	}

	public List<PositionWithLink> generateLinksWithLength(final List<PositionWithIdAndType> positions) {
		final List<PositionWithLink> newPositions = new ArrayList<>(positions.size() * 2);

		final ImmutableBeatsMap beats = chartData.beats();
		for (final PositionWithIdAndType position : positions) {
			newPositions.add(new PositionWithLink(position.toPosition(beats).position(), position));
			newPositions.add(new PositionWithLink(position.endPosition().toPosition(beats).position(), position));
		}

		return newPositions;
	}

	private PositionWithIdAndType findExisting(final int x, final List<PositionWithLink> positionsWithLinks) {
		final double position = xToPosition(x, chartTimeHandler.time());
		final PositionWithLink closestLink = closest(positionsWithLinks, new ConstantPosition(position)).find();
		if (closestLink == null) {
			return null;
		}

		final PositionWithIdAndType closest = closestLink.link;
		final int closestX = chartTimeHandler.positionToX(closest.asConstantPosition().position());
		if (x - closestX < -20 || x - closestX > 20) {
			return null;
		}

		return closest;
	}

	private PositionWithIdAndType findWithLengthExisting(final int x, final List<PositionWithLink> positionsWithLinks) {
		final double position = xToPosition(x, chartTimeHandler.time());
		final PositionWithLink closestLink = closest(positionsWithLinks, new ConstantPosition(position)).find();
		if (closestLink == null) {
			return null;
		}

		final PositionWithIdAndType closest = closestLink.link;
		final int closestX = chartTimeHandler.positionToX(closest.asConstantPosition().position());
		final int closestEndX = chartTimeHandler.positionToX(closest.endPosition().asConstantPosition().position());
		if (x - closestX < -20 || x - closestEndX > 20) {
			return null;
		}

		return closest;
	}

	public PositionWithIdAndType findExistingPosition(final int x, final int y) {
		final PositionType positionType = PositionType.fromY(y, modeManager.getMode());
		final List<PositionWithIdAndType> positions = positionType.getPositionsWithIdsAndTypes(chartData);

		if (positionType == PositionType.VOCAL//
				|| positionType == PositionType.HAND_SHAPE//
				|| (positionType == PositionType.GUITAR_NOTE && selectNotesByTails)) {
			return findWithLengthExisting(x, generateLinksWithLength(positions));
		}

		return findExisting(x, generateLinks(positions));
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

		return (ISelectionAccessor<T>) selectionList.getAccessor();
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

	public PositionType selectedType() {
		for (final SelectionList<?, ?, ?> selectionList : selectionLists.values()) {
			if (selectionList.getAccessor().isSelected()) {
				return selectionList.type;
			}
		}

		return PositionType.NONE;
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

	public void selectAll() {
		final PositionType positionTypeToSelect = switch (modeManager.getMode()) {
			case VOCALS -> PositionType.VOCAL;
			case GUITAR -> {
				PositionType positionType = selectedType();
				if (positionType != PositionType.NONE) {
					yield positionType;
				}

				positionType = mouseHandler.getMouseHoverPositionType();
				yield positionType == PositionType.NONE ? PositionType.GUITAR_NOTE : positionType;
			}
			default -> PositionType.NONE;
		};

		selectionLists.get(positionTypeToSelect).addAll();
		currentSelectionEditor.selectionChanged(true);
	}

	public void addSelection(final PositionType type, final int id) {
		selectionLists.get(type).add(id);
		currentSelectionEditor.selectionChanged(true);
	}

	public void addSoundSelection(final int id) {
		addSelection(PositionType.GUITAR_NOTE, id);
	}

	public void addSoundSelection(final List<Integer> ids) {
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
