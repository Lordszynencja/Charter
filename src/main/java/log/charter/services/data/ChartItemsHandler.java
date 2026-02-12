package log.charter.services.data;

import static java.util.Arrays.asList;
import static log.charter.util.CollectionUtils.getFromTo;
import static log.charter.util.CollectionUtils.map;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import log.charter.data.ChartData;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.EventPoint;
import log.charter.data.song.FHP;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.song.position.virtual.IVirtualConstantPosition;
import log.charter.data.song.position.virtual.IVirtualPosition;
import log.charter.data.song.position.virtual.IVirtualPositionWithEnd;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.fixers.LinkedNotesFixer;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.util.CollectionUtils;

public class ChartItemsHandler {

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void delete() {
		final ISelectionAccessor<IVirtualConstantPosition> selectedAccessor = selectionManager.selectedAccessor();
		if (selectedAccessor.type() == PositionType.NONE || selectedAccessor.type() == PositionType.BEAT) {
			return;
		}
		if (!selectedAccessor.isSelected()) {
			return;
		}

		final List<Selection<IVirtualConstantPosition>> selected = selectedAccessor.getSelected();

		undoSystem.addUndo();
		selectionManager.clear();
		delete(selectedAccessor.type(), selected.stream().map(selection -> selection.id).collect(Collectors.toList()));
	}

	private void deleteFromTo(final List<? extends IConstantFractionalPosition> items,
			final IConstantFractionalPosition from, final IConstantFractionalPosition to) {
		final Integer fromId = CollectionUtils.firstAfterEqual(items, from).findId();
		final Integer toId = CollectionUtils.lastBeforeEqual(items, to).findId();
		if (fromId == null || toId == null) {
			return;
		}

		for (int i = toId; i >= fromId; i--) {
			items.remove(i);
		}
	}

	private void deleteRelatedToNotes(final PositionType type, final List<IVirtualConstantPosition> selectedElements) {
		final FractionalPosition selectionStart = selectedElements.get(0).toFraction(chartData.beats()).position();
		final IVirtualConstantPosition lastElement = selectedElements.get(selectedElements.size() - 1);
		FractionalPosition selectionEnd;
		if (lastElement instanceof FHP) {
			selectionEnd = ((FHP) lastElement).position();
		} else if (lastElement instanceof ChordOrNote) {
			selectionEnd = ((ChordOrNote) lastElement).endPosition();
		} else if (lastElement instanceof HandShape) {
			selectionEnd = ((HandShape) lastElement).endPosition();
		} else {
			selectionEnd = lastElement.toFraction(chartData.beats()).position();
		}

		undoSystem.addUndo();
		selectionManager.clear();

		deleteFromTo(chartData.currentFHPs(), selectionStart, selectionEnd);
		deleteFromTo(chartData.currentSounds(), selectionStart, selectionEnd);
		deleteFromTo(chartData.currentHandShapes(), selectionStart, selectionEnd);

		arrangementFixer.fixNoteLengths(chartData.currentSounds());
	}

	public void deleteRelated() {
		final ISelectionAccessor<IVirtualConstantPosition> selectedAccessor = selectionManager.selectedAccessor();
		if (!selectedAccessor.isSelected()) {
			return;
		}

		switch (selectedAccessor.type()) {
			case FHP:
			case GUITAR_NOTE:
			case HAND_SHAPE:
				deleteRelatedToNotes(selectedAccessor.type(), selectedAccessor.getSelectedElements());
				break;
			default:
				delete();
				break;
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
					.collect(Collectors.toCollection(HashSet::new));
		}

		switch (type) {
			case GUITAR_NOTE:
				arrangementFixer.fixNoteLengths(chartData.currentSounds());
				break;
			default:
				break;
		}
	}

	private <T extends IVirtualPosition> void snapPositions(final Stream<T> positions, final List<T> allPositions) {
		positions.forEach(p -> chartData.beats().snap(p));
		clearRepeatedPositions(allPositions);
	}

	private <T extends IVirtualConstantPosition> void clearRepeatedPositions(final List<T> positions) {
		if (positions.isEmpty()) {
			return;
		}

		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(chartData.beats());
		for (int i = positions.size() - 1; i > 0; i--) {
			if (comparator.compare(positions.get(i), positions.get(i - 1)) == 0) {
				positions.remove(i);
			}
		}
	}

	private void snapNotePositions(final Stream<ChordOrNote> positions) {
		final List<ChordOrNote> sounds = chartData.currentSounds();
		final List<BendValue> bends = sounds.stream()//
				.flatMap(c -> c.isNote() ? c.note().bendValues.stream()
						: c.chord().chordNotes.values().stream().flatMap(n -> n.bendValues.stream()))//
				.collect(Collectors.toList());

		snapPositions(bends.stream(), bends);
		snapPositions(positions, sounds);
		arrangementFixer.fixNoteLengths(sounds);
	}

	private <T extends IVirtualPositionWithEnd> void snapPositionsWithLength(final Stream<T> positions,
			final List<T> allPositions) {
		snapPositions(positions, allPositions);
		arrangementFixer.fixLengths(allPositions);
	}

	private <C extends IVirtualConstantPosition> void reselectAfterSnapping(final PositionType type,
			final Collection<Selection<C>> selected) {
		final List<C> selectedPositions = map(selected, s -> s.selectable);

		selectionManager.clear();
		selectionManager.addSelectionForPositions(type, selectedPositions);
	}

	public <T extends IVirtualPosition> void snapSelected() {
		final ISelectionAccessor<T> accessor = selectionManager.selectedAccessor();
		if (!accessor.isSelected() || asList(PositionType.BEAT, PositionType.NONE).contains(accessor.type())) {
			return;
		}

		undoSystem.addUndo();

		final List<Selection<T>> selected = accessor.getSelected();
		switch (accessor.type()) {
			case EVENT_POINT:
				snapPositions(selected.stream().map(selection -> (EventPoint) selection.selectable),
						chartData.currentEventPoints());
				clearRepeatedPositions(chartData.currentEventPoints());
				break;
			case FHP:
				snapPositions(selected.stream().map(selection -> (FHP) selection.selectable), chartData.currentFHPs());
				break;
			case TONE_CHANGE:
				snapPositions(selected.stream().map(selection -> (ToneChange) selection.selectable),
						chartData.currentToneChanges());
				break;
			case GUITAR_NOTE:
				snapNotePositions(selected.stream().map(selection -> (ChordOrNote) selection.selectable));
				break;
			case HAND_SHAPE:
				snapPositionsWithLength(selected.stream().map(selection -> (HandShape) selection.selectable),
						chartData.currentArrangementLevel().handShapes);
				break;
			case VOCAL:
				snapPositionsWithLength(selected.stream().map(selection -> (Vocal) selection.selectable),
						chartData.currentVocals().vocals);
				break;
			default:
				break;
		}

		reselectAfterSnapping(accessor.type(), selected);
	}

	private void snapAllOnVocals(final ISelectionAccessor<Vocal> accessor) {
		undoSystem.addUndo();

		final List<Selection<Vocal>> selected = accessor.getSelected();
		final FractionalPosition from = new FractionalPosition(
				selected.get(0).selectable.toFraction(chartData.beats()).position());
		final FractionalPosition to = new FractionalPosition(
				selected.get(selected.size() - 1).selectable.toFraction(chartData.beats()).position());
		snapPositionsWithLength(getFromTo(chartData.currentVocals().vocals, from, to).stream(),
				chartData.currentVocals().vocals);

		reselectAfterSnapping(accessor.type(), selected);
	}

	private <T extends IVirtualPosition> void snapAllOnGuitar(final ISelectionAccessor<T> accessor) {
		undoSystem.addUndo();

		final List<Selection<T>> selected = accessor.getSelected();
		final FractionalPosition from = new FractionalPosition(
				selected.get(0).selectable.toFraction(chartData.beats()).position());
		final FractionalPosition to = new FractionalPosition(
				selected.get(selected.size() - 1).selectable.toFraction(chartData.beats()).position());
		final Arrangement arrangement = chartData.currentArrangement();
		final Level level = chartData.currentArrangementLevel();
		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(chartData.beats());

		snapPositions(getFromTo(arrangement.eventPoints, from, to, comparator).stream(), arrangement.eventPoints);
		snapPositions(getFromTo(arrangement.toneChanges, from, to, comparator).stream(), arrangement.toneChanges);
		snapPositions(getFromTo(level.fhps, from, to, comparator).stream(), level.fhps);
		snapNotePositions(getFromTo(level.sounds, from, to, comparator).stream());
		snapPositionsWithLength(getFromTo(level.handShapes, from, to, comparator).stream(), level.handShapes);

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
		final List<Selection<ChordOrNote>> selected = selectionManager.getSelected(PositionType.GUITAR_NOTE);

		for (final Selection<ChordOrNote> selection : selected) {
			sounds.set(selection.id, mapper.apply(selection.selectable));
		}
	}

	private <P extends IVirtualPositionWithEnd> void changePositionLength(final ImmutableBeatsMap beats,
			final P position, final int gridsChange) {
		position.endPosition(beats, beats.addGrid(position.endPosition(), gridsChange));
	}

	public <P extends IVirtualPositionWithEnd> void changePositionsWithLengthsByGrid(final List<P> toChange,
			final List<P> allPositions, final int gridsChange) {
		final ImmutableBeatsMap beats = chartData.beats();
		for (final P selected : toChange) {
			changePositionLength(beats, selected, gridsChange);
		}

		arrangementFixer.fixLengths(allPositions);
	}

	private void changeNoteLength(final ImmutableBeatsMap beats, final List<ChordOrNote> sounds, final CommonNote note,
			final int id, final int gridsChange) {
		if (note.linkNext()) {
			LinkedNotesFixer.fixLinkedNote(note, id, sounds);
			return;
		}

		IVirtualConstantPosition endPosition = note.endPosition();
		endPosition = beats.addGrid(endPosition, gridsChange);
		note.endPosition(endPosition.toFraction(beats).position());

		arrangementFixer.fixNoteLengthWithoutCutting(note, id, sounds);
	}

	public void changeSoundsLength(final List<Selection<ChordOrNote>> toChange, final int gridsChange,
			final List<Integer> selectedStrings) {
		final ImmutableBeatsMap beats = chartData.beats();
		final List<ChordOrNote> sounds = chartData.currentSounds();

		for (final Selection<ChordOrNote> selected : toChange) {
			selected.selectable.notes().forEach(note -> {
				if (!selectedStrings.contains(note.string())) {
					return;
				}

				changeNoteLength(beats, sounds, note, selected.id, gridsChange);
			});
		}
	}
}
