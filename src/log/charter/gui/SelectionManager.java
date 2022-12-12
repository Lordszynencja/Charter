package log.charter.gui;

import static log.charter.util.ScalingUtils.timeToX;
import static log.charter.util.ScalingUtils.xToTime;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.ChartData;
import log.charter.gui.PositionWithIdAndType.PositionType;
import log.charter.song.Beat;
import log.charter.song.Chord;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.Note;
import log.charter.song.Position;
import log.charter.song.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;
import log.charter.util.IntRange;

public class SelectionManager {
	public static interface Selectable {
		public String getSignature();
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
		BEAT, CHORD, HAND_SHAPE, NOTE, VOCAL;
	}

	private ChartData data;

	private SelectionTypeEnum lastSelectedValue = null;

	private final ArrayList2<Selection<Beat>> selectedBeats = new ArrayList2<>();
	private final ArrayList2<Selection<Chord>> selectedChords = new ArrayList2<>();
	private final ArrayList2<Selection<HandShape>> selectedHandShapes = new ArrayList2<>();
	private final ArrayList2<Selection<Note>> selectedNotes = new ArrayList2<>();
	private final ArrayList2<Selection<Vocal>> selectedVocals = new ArrayList2<>();

	public void init(final ChartData data) {
		this.data = data;
	}

	private <T extends Position & Selectable> ArrayList2<Selection<T>> getSortedCopy(
			final ArrayList2<Selection<T>> list) {
		final ArrayList2<Selection<T>> copy = new ArrayList2<>(list);
		copy.sort((selection0, selection1) -> Integer.compare(selection0.selectable.position,
				selection1.selectable.position));

		return copy;
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

	private <T extends Position & Selectable> void addSelectables(final ArrayList2<Selection<T>> selections,
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

	private <T extends Position & Selectable> void addSelectables(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, int toId) {
		int fromId = selections.isEmpty() ? toId : selections.getLast().id;
		if (fromId > toId) {
			final int tmp = fromId;
			fromId = toId;
			toId = tmp;
		}

		addSelectables(selections, available, fromId, toId);
	}

	private <T extends Position & Selectable> void addSelectablesFromToPosition(
			final ArrayList2<Selection<T>> selections, final ArrayList2<T> available, final int fromPosition,
			final int toPosition) {
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

	private <T extends Position & Selectable> void switchSelectable(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, final int id) {
		if (selections.contains(selection -> selection.id == id)) {
			selections.removeIf(selection -> selection.id == id);
			return;
		}

		selections.add(new Selection<>(id, available.get(id)));
	}

	private <T extends Position & Selectable> void setSelectable(final ArrayList2<Selection<T>> selections,
			final ArrayList2<T> available, final int id) {
		selections.clear();
		selections.add(new Selection<>(id, available.get(id)));
	}

	private void clearBeatSelection() {
		if (!selectedBeats.isEmpty()) {
			selectedBeats.clear();
		}
	}

	private void clearGuitarNotesSelection() {
		if (!selectedChords.isEmpty()) {
			selectedChords.clear();
		}
		if (!selectedNotes.isEmpty()) {
			selectedNotes.clear();
		}
	}

	private void clearHandShapesSelection() {
		if (!selectedHandShapes.isEmpty()) {
			selectedHandShapes.clear();
		}
	}

	private void clearVocalsSelection() {
		if (!selectedVocals.isEmpty()) {
			selectedVocals.clear();
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
		final PositionType positionType = PositionType.fromY(y, data);
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

	private <T extends Position & Selectable> void addSelectablesWithModifiers(
			final ArrayList2<Selection<T>> selections, final ArrayList2<T> available, final int id, final boolean ctrl,
			final boolean shift) {
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
			clearBeatSelection();
			clearGuitarNotesSelection();
			clearHandShapesSelection();
			clearVocalsSelection();
		}
	}

	private void positionBeat(final PositionWithIdAndType closestPosition, final boolean ctrl, final boolean shift) {
		clearGuitarNotesSelection();
		clearHandShapesSelection();
		clearVocalsSelection();

		addSelectablesWithModifiers(selectedBeats, data.songChart.beatsMap.beats, closestPosition.id, ctrl, shift);
		lastSelectedValue = SelectionTypeEnum.BEAT;
	}

	private void positionGuitarNote(final PositionWithIdAndType closestPosition, final boolean ctrl,
			final boolean shift) {
		clearBeatSelection();
		clearHandShapesSelection();
		clearVocalsSelection();

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

		clearGuitarNotesSelection();

		if (closestPosition.chord != null) {
			setSelectable(selectedChords, level.chords, closestPosition.id);
			lastSelectedValue = SelectionTypeEnum.CHORD;
			return;
		}

		setSelectable(selectedNotes, level.notes, closestPosition.id);
		lastSelectedValue = SelectionTypeEnum.NOTE;
	}

	private void positionHandShape(final PositionWithIdAndType closestPosition, final boolean ctrl,
			final boolean shift) {
		clearBeatSelection();
		clearGuitarNotesSelection();
		clearVocalsSelection();

		addSelectablesWithModifiers(selectedHandShapes, data.getCurrentArrangementLevel().handShapes,
				closestPosition.id, ctrl, shift);
		lastSelectedValue = SelectionTypeEnum.HAND_SHAPE;
	}

	private void positionVocals(final PositionWithIdAndType closestPosition, final boolean ctrl, final boolean shift) {
		clearBeatSelection();
		clearHandShapesSelection();
		clearGuitarNotesSelection();

		addSelectablesWithModifiers(selectedVocals, data.songChart.vocals.vocals, closestPosition.id, ctrl, shift);
		lastSelectedValue = SelectionTypeEnum.VOCAL;
	}

	public void click(final int x, final int y, final boolean ctrl, final boolean shift) {
		if (data.isEmpty) {
			return;
		}

		final PositionWithIdAndType closestPosition = findExistingPosition(x, y);
		if (closestPosition == null) {
			positionNotFound(ctrl);
			return;
		}

		switch (closestPosition.type) {
		case BEAT:
			positionBeat(closestPosition, ctrl, shift);
			break;
		case GUITAR_NOTE:
			positionGuitarNote(closestPosition, ctrl, shift);
			break;
		case HAND_SHAPE:
			positionHandShape(closestPosition, ctrl, shift);
			break;
		case VOCAL:
			positionVocals(closestPosition, ctrl, shift);
			break;
		default:
			break;
		}
	}
}
