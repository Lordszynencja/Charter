package log.charter.data.managers;

import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import log.charter.data.ChartData;
import log.charter.data.PositionWithIdAndType;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.song.Anchor;
import log.charter.song.Beat;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.song.Position;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.IntRange;

public class SelectionManager {
	public abstract static class Selectable extends Position {
		public Selectable(final int position) {
			super(position);
		}

		public Selectable(final Selectable position) {
			super(position);
		}

		public abstract String getSignature();
	}

	public static class Selection<T extends Selectable> {
		public final int id;
		public final T selectable;

		public Selection(final int id, final T selectable) {
			this.id = id;
			this.selectable = selectable;
		}
	}

	private enum SelectionTypeEnum {
		ANCHOR, //
		BEAT, //
		CHORD, //
		HAND_SHAPE, //
		NOTE, //
		VOCAL;

		public static SelectionTypeEnum fromPositionType(final PositionType type) {
			switch (type) {
			case ANCHOR:
				return ANCHOR;
			case BEAT:
				return BEAT;
			case GUITAR_NOTE:
				return null;
			case HAND_SHAPE:
				return HAND_SHAPE;
			case NONE:
				return null;
			case VOCAL:
				return VOCAL;
			default:
				return null;
			}
		}
	}

	private ChartData data;
	private ModeManager modeManager;

	private SelectionTypeEnum lastSelectedValue = null;

	private final ArrayList2<Selection<Anchor>> selectedAnchors = new ArrayList2<>();
	private final ArrayList2<Selection<Beat>> selectedBeats = new ArrayList2<>();
	private final ArrayList2<Selection<Chord>> selectedChords = new ArrayList2<>();
	private final ArrayList2<Selection<HandShape>> selectedHandShapes = new ArrayList2<>();
	private final ArrayList2<Selection<Note>> selectedNotes = new ArrayList2<>();
	private final ArrayList2<Selection<Vocal>> selectedVocals = new ArrayList2<>();

	public void init(final ChartData data, final ModeManager modeManager) {
		this.data = data;
		this.modeManager = modeManager;
	}

	private <T extends Selectable> ArrayList2<Selection<T>> getSortedCopy(final ArrayList2<Selection<T>> list) {
		final ArrayList2<Selection<T>> copy = new ArrayList2<>(list);
		copy.sort((selection0, selection1) -> Integer.compare(selection0.selectable.position,
				selection1.selectable.position));

		return copy;
	}

	public ArrayList2<Selection<Anchor>> getSelectedAnchors() {
		return getSortedCopy(selectedAnchors);
	}

	public HashSet2<Selection<Anchor>> getSelectedAnchorsSet() {
		return new HashSet2<>(selectedAnchors);
	}

	public ArrayList2<Selection<Beat>> getSelectedBeats() {
		return getSortedCopy(selectedBeats);
	}

	public HashSet2<Selection<Beat>> getSelectedBeatsSet() {
		return new HashSet2<>(selectedBeats);
	}

	public ArrayList2<Selection<Chord>> getSelectedChords() {
		return getSortedCopy(selectedChords);
	}

	public HashSet2<Selection<Chord>> getSelectedChordsSet() {
		return new HashSet2<>(selectedChords);
	}

	public ArrayList2<Selection<HandShape>> getSelectedHandShapes() {
		return getSortedCopy(selectedHandShapes);
	}

	public HashSet2<Selection<HandShape>> getSelectedHandShapesSet() {
		return new HashSet2<>(selectedHandShapes);
	}

	public ArrayList2<Selection<Note>> getSelectedNotes() {
		return getSortedCopy(selectedNotes);
	}

	public HashSet2<Selection<Note>> getSelectedNotesSet() {
		return new HashSet2<>(selectedNotes);
	}

	public ArrayList2<Selection<Vocal>> getSelectedVocals() {
		return getSortedCopy(selectedVocals);
	}

	public HashSet2<Selection<Vocal>> getSelectedVocalsSet() {
		return new HashSet2<>(selectedVocals);
	}

	private <T extends Selectable> void addSelectables(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, final int fromId, final int toId) {
		final Set<String> selectedSignatures = new HashSet<>(
				selections.map(selection -> selection.selectable.getSignature()));

		for (int i = fromId; i <= toId; i++) {
			final T selectable = available.get(i);
			if (!selectedSignatures.contains(selectable.getSignature())) {
				selections.add(new Selection<>(i, selectable));
			}
		}
	}

	private <T extends Selectable> void addSelectables(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, int toId) {
		int fromId = selections.isEmpty() ? toId : selections.getLast().id;
		if (fromId > toId) {
			final int tmp = fromId;
			fromId = toId;
			toId = tmp;
		}

		addSelectables(selections, available, fromId, toId);
	}

	private <T extends Selectable> void addSelectablesFromToPosition(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, final int fromPosition, final int toPosition) {
		final Set<String> selectedSignatures = new HashSet<>(
				selections.map(selection -> selection.selectable.getSignature()));

		for (int i = 0; i < available.size(); i++) {
			final T selectable = available.get(i);
			if (selectable.position >= fromPosition && selectable.position <= toPosition
					&& !selectedSignatures.contains(selectable.getSignature())) {
				selections.add(new Selection<>(i, selectable));
			}
		}
	}

	private <T extends Selectable> void switchSelectable(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, final int id) {
		if (selections.contains(selection -> selection.id == id)) {
			selections.removeIf(selection -> selection.id == id);
			return;
		}

		selections.add(new Selection<>(id, available.get(id)));
	}

	private <T extends Selectable> void setSelectable(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, final int id) {
		selections.clear();
		selections.add(new Selection<>(id, available.get(id)));
	}

	private void clearList(final ArrayList2<?> list) {
		if (!list.isEmpty()) {
			list.clear();
		}
	}

	private void clearSelectionsExcept(final PositionType type) {
		if (type != PositionType.ANCHOR) {
			clearList(selectedAnchors);
		}
		if (type != PositionType.BEAT) {
			clearList(selectedBeats);
		}
		if (type != PositionType.GUITAR_NOTE) {
			clearList(selectedChords);
			clearList(selectedNotes);
		}
		if (type != PositionType.HAND_SHAPE) {
			clearList(selectedHandShapes);
		}
		if (type != PositionType.VOCAL) {
			clearList(selectedVocals);
		}
	}

	private static class PositionWithLink extends Position {
		public static ArrayList2<PositionWithLink> fromPositionsWithIdAndType(
				final ArrayList2<PositionWithIdAndType> positions) {
			final ArrayList2<PositionWithLink> newPositions = new ArrayList2<>(positions.size() * 2);

			for (final PositionWithIdAndType position : positions) {
				newPositions.add(new PositionWithLink(position.position, position));
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

	public PositionWithIdAndType findExistingPosition(final int x, final int y) {
		final PositionType positionType = PositionType.fromY(y, modeManager.editMode);
		final ArrayList2<PositionWithIdAndType> positions = positionType.positionChooser
				.getAvailablePositionsForSelection(data);

		final ArrayList2<PositionWithLink> positionsWithLinks = PositionWithLink.fromPositionsWithIdAndType(positions);
		final int position = xToTime(x, data.time);
		final Integer id = Position.findClosest(positionsWithLinks, position);
		if (id == null) {
			return null;
		}

		final PositionWithIdAndType closest = positionsWithLinks.get(id).link;
		if (x - timeToX(closest.position, data.time) < -20 || x - timeToX(closest.endPosition, data.time) > 20) {
			return null;
		}

		return closest;
	}

	private <T extends Selectable> void addSelectablesWithModifiers(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, final int id, final boolean ctrl, final boolean shift) {
		if (ctrl) {
			switchSelectable(selections, available, id);
			return;
		}

		if (shift) {
			addSelectables(selections, available, id);
			return;
		}

		setSelectable(selections, available, id);
	}

	private void positionNotFound(final boolean ctrl) {
		if (!ctrl) {
			clearSelectionsExcept(PositionType.NONE);
		}
	}

	private void positionGuitarNote(final PositionWithIdAndType closestPosition, final boolean ctrl,
			final boolean shift) {
		final Level level = data.getCurrentArrangementLevel();
		if (ctrl) {
			if (closestPosition.chord != null) {
				switchSelectable(selectedChords, level.chords, closestPosition.id);
				lastSelectedValue = SelectionTypeEnum.CHORD;
				return;
			}

			switchSelectable(selectedNotes, level.notes, closestPosition.id);
			lastSelectedValue = SelectionTypeEnum.NOTE;
			return;
		}

		if (shift) {
			final ArrayList2<?> positionsToSeekLast = //
					lastSelectedValue == SelectionTypeEnum.NOTE //
							? selectedNotes//
							: selectedChords;
			int fromPosition = closestPosition.position;
			if (!positionsToSeekLast.isEmpty()) {
				fromPosition = ((Position) ((Selection<?>) (positionsToSeekLast).getLast()).selectable).position;
			}
			final IntRange seekRange = new IntRange(fromPosition, closestPosition.position);

			addSelectablesFromToPosition(selectedChords, level.chords, seekRange.min, seekRange.max);
			addSelectablesFromToPosition(selectedNotes, level.notes, seekRange.min, seekRange.max);

			return;
		}

		clearList(selectedChords);
		clearList(selectedNotes);

		if (closestPosition.chord != null) {
			setSelectable(selectedChords, level.chords, closestPosition.id);
			lastSelectedValue = SelectionTypeEnum.CHORD;
			return;
		}

		setSelectable(selectedNotes, level.notes, closestPosition.id);
		lastSelectedValue = SelectionTypeEnum.NOTE;
	}

	private static class SelectableSupplier<T extends Selectable> {
		public final Supplier<ArrayList2<Selection<T>>> selection;
		public final Supplier<ArrayList2<T>> available;

		private SelectableSupplier(final Supplier<ArrayList2<Selection<T>>> selection,
				final Supplier<ArrayList2<T>> available) {
			this.selection = selection;
			this.available = available;
		}
	}

	private final Map<PositionType, SelectableSupplier<? extends Selectable>> selectableSuppliers = new HashMap2<>();

	{
		selectableSuppliers.put(PositionType.ANCHOR, //
				new SelectableSupplier<>(() -> selectedAnchors, () -> data.getCurrentArrangementLevel().anchors));
		selectableSuppliers.put(PositionType.BEAT, //
				new SelectableSupplier<>(() -> selectedBeats, () -> data.songChart.beatsMap.beats));
		selectableSuppliers.put(PositionType.HAND_SHAPE, //
				new SelectableSupplier<>(() -> selectedHandShapes, () -> data.getCurrentArrangementLevel().handShapes));
		selectableSuppliers.put(PositionType.VOCAL, //
				new SelectableSupplier<Vocal>(() -> selectedVocals, () -> data.songChart.vocals.vocals));
	}

	@SuppressWarnings("unchecked")
	private <T extends Selectable> SelectableSupplier<T> getSelectableSupplier(final PositionType type) {
		return (SelectableSupplier<T>) selectableSuppliers.get(type);
	}

	public <T extends Selectable> void click(final int x, final int y, final boolean ctrl, final boolean shift) {
		if (data.isEmpty) {
			return;
		}

		final PositionWithIdAndType closestPosition = findExistingPosition(x, y);
		if (closestPosition == null) {
			positionNotFound(ctrl);
			return;
		}

		clearSelectionsExcept(closestPosition.type);
		switch (closestPosition.type) {
		case ANCHOR:
		case BEAT:
		case HAND_SHAPE:
		case VOCAL:
			final SelectableSupplier<T> supplier = getSelectableSupplier(closestPosition.type);
			addSelectablesWithModifiers(supplier.selection.get(), supplier.available.get(), closestPosition.id, ctrl,
					shift);
			lastSelectedValue = SelectionTypeEnum.fromPositionType(closestPosition.type);
			break;
		case GUITAR_NOTE:
			positionGuitarNote(closestPosition, ctrl, shift);
			break;
		default:
			break;
		}
	}

	public void clear() {
		clearSelectionsExcept(PositionType.NONE);
	}
}
