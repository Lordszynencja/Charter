package log.charter.services.data;

import static java.util.Arrays.asList;
import static log.charter.util.CollectionUtils.getFromTo;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.IPositionWithLength;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.song.position.IVirtualPosition;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.util.collections.HashSet2;

public class ChartItemsHandler {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public <T extends IVirtualConstantPosition & Comparable<? super T>> void delete() {
		boolean nonEmptyFound = false;

		for (final PositionType type : PositionType.values()) {
			if (type == PositionType.NONE || type == PositionType.BEAT) {
				continue;
			}

			final ISelectionAccessor<T> selectedTypeAccessor = selectionManager.accessor(type);
			if (!selectedTypeAccessor.isSelected()) {
				continue;
			}

			final List<Selection<T>> selected = selectedTypeAccessor.getSortedSelected();
			if (!nonEmptyFound) {
				undoSystem.addUndo();
				selectionManager.clear();
				nonEmptyFound = true;
			}

			delete(type, selected.stream().map(selection -> selection.id).collect(Collectors.toList()));
		}
	}

	public void delete(final PositionType type, final List<Integer> idsToDelete) {
		if (type == PositionType.NONE || type == PositionType.BEAT//
				|| idsToDelete.isEmpty()) {
			return;
		}

		idsToDelete.sort((a, b) -> -Integer.compare(a, b));
		final List<?> items = type.manager().getItems(chartData);
		idsToDelete.forEach(id -> items.remove((int) id));

		if (type == PositionType.TONE_CHANGE) {
			chartData.currentArrangement().tones = chartData.currentArrangement().toneChanges.stream()//
					.map(toneChange -> toneChange.toneName)//
					.collect(Collectors.toCollection(HashSet2::new));
		}
	}

	private <T extends IVirtualPosition> void snapPositions(final Stream<T> positions) {
		positions.forEach(p -> chartData.beats().snap(p));
	}

	private <T extends IVirtualConstantPosition> void clearRepeatedPositions(final List<T> positions) {
		if (positions.isEmpty()) {
			return;
		}

		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(chartData.beats());
		for (int i = positions.size() - 1; i > 0; i++) {
			if (comparator.compare(positions.get(i), positions.get(i - 1)) == 0) {
				positions.remove(i);
			}
		}
	}

	private void snapNotePositions(final Stream<ChordOrNote> positions) {
		snapPositions(positions);

		final List<ChordOrNote> sounds = chartData.currentSounds();
		clearRepeatedPositions(sounds);
		arrangementFixer.fixNoteLengths(sounds);
	}

	private <T extends IPositionWithLength> void snapPositionsWithLength(final Collection<T> positions,
			final List<T> allPositions) {
		snapPositions(positions.stream());
		clearRepeatedPositions(allPositions);
		arrangementFixer.fixLengths(allPositions);
	}

	private <C extends IVirtualConstantPosition, P extends C, T extends P> void reselectAfterSnapping(
			final PositionType type, final Collection<Selection<T>> selected) {
		final List<C> selectedPositions = type.<C, P, T>manager().asConstant(selected.stream().map(s -> s.selectable));

		selectionManager.clear();
		selectionManager.addSelectionForPositions(type, selectedPositions);
	}

	public <T extends IVirtualPosition> void snapSelected() {
		final ISelectionAccessor<T> accessor = selectionManager.selectedAccessor();
		if (!accessor.isSelected() || asList(PositionType.BEAT, PositionType.NONE).contains(accessor.type())) {
			return;
		}

		undoSystem.addUndo();

		final Set<Selection<T>> selected = accessor.getSelectedSet();
		switch (accessor.type()) {
			case EVENT_POINT:
				snapPositions(selected.stream().map(selection -> selection.selectable));
			case ANCHOR:
				snapPositions(selected.stream().map(selection -> selection.selectable));
			case TONE_CHANGE:
				snapPositions(selected.stream().map(selection -> selection.selectable));
				break;
			case GUITAR_NOTE:
				snapNotePositions(selected.stream().map(selection -> (ChordOrNote) selection.selectable));
				break;
			case HAND_SHAPE:
				snapPositionsWithLength(selected.stream().map(selection -> (HandShape) selection.selectable)
						.collect(Collectors.toList()), chartData.currentArrangementLevel().handShapes);
				break;
			case VOCAL:
				snapPositionsWithLength(
						selected.stream().map(selection -> (Vocal) selection.selectable).collect(Collectors.toList()),
						chartData.songChart.vocals.vocals);
				break;
			default:
				break;
		}

		reselectAfterSnapping(accessor.type(), selected);
	}

	private void snapAllOnVocals(final ISelectionAccessor<Vocal> accessor) {
		undoSystem.addUndo();

		final List<Selection<Vocal>> selected = accessor.getSortedSelected();
		final Vocal from = selected.get(0).selectable;
		final Vocal to = selected.get(selected.size() - 1).selectable;
		snapPositionsWithLength(getFromTo(chartData.currentVocals().vocals, from, to),
				chartData.currentVocals().vocals);

		reselectAfterSnapping(accessor.type(), selected);
	}

	private <T extends IVirtualPosition> void snapAllOnGuitar(final ISelectionAccessor<T> accessor) {
		undoSystem.addUndo();

		final List<Selection<T>> selected = accessor.getSortedSelected();
		final T from = selected.get(0).selectable;
		final T to = selected.get(selected.size() - 1).selectable;
		final Arrangement arrangement = chartData.currentArrangement();
		final Level level = chartData.currentArrangementLevel();
		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(chartData.beats());

		snapPositions(getFromTo(arrangement.eventPoints, from, to, comparator).stream());
		snapPositions(getFromTo(arrangement.toneChanges, from, to, comparator).stream());
		snapPositions(getFromTo(level.anchors, from, to, comparator).stream());
		snapNotePositions(getFromTo(level.sounds, from, to, comparator).stream());
		snapPositionsWithLength(getFromTo(level.handShapes, from, to, comparator), level.handShapes);

		reselectAfterSnapping(accessor.type(), selected);
	}

	@SuppressWarnings("unchecked")
	public <T extends IVirtualPosition> void snapAll() {
		final ISelectionAccessor<T> accessor = selectionManager.selectedAccessor();
		if (!accessor.isSelected()) {
			return;
		}

		switch (modeManager.getMode()) {
			case GUITAR:
				snapAllOnGuitar(accessor);
				break;
			case VOCALS:
				if (accessor.type() == PositionType.VOCAL) {
					snapAllOnVocals((ISelectionAccessor<Vocal>) accessor);
				}
				break;
			case TEMPO_MAP:
			case EMPTY:
			default:
				break;
		}
	}

	public void mapSounds(final Function<ChordOrNote, ChordOrNote> mapper) {
		final List<ChordOrNote> sounds = chartData.currentArrangementLevel().sounds;
		final ISelectionAccessor<ChordOrNote> selectionAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		final Set<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		for (final Selection<ChordOrNote> selection : selected) {
			sounds.set(selection.id, mapper.apply(selection.selectable));
		}
	}
}
