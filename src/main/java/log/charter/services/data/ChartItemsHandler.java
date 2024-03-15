package log.charter.services.data;

import static java.util.Arrays.asList;
import static log.charter.data.config.Config.minNoteDistance;
import static log.charter.data.song.position.IVirtualConstantPosition.add;
import static log.charter.services.data.fixers.ArrangementFixer.fixNoteLength;
import static log.charter.util.CollectionUtils.getFromTo;
import static log.charter.util.CollectionUtils.map;
import static log.charter.util.CollectionUtils.min;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import log.charter.data.ChartData;
import log.charter.data.song.Anchor;
import log.charter.data.song.Arrangement;
import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.EventPoint;
import log.charter.data.song.HandShape;
import log.charter.data.song.Level;
import log.charter.data.song.ToneChange;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.position.ConstantPosition;
import log.charter.data.song.position.IVirtualConstantPosition;
import log.charter.data.song.position.IVirtualPosition;
import log.charter.data.song.position.IVirtualPositionWithEnd;
import log.charter.data.song.position.Position;
import log.charter.data.song.vocals.Vocal;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.fixers.LinkedNotesFixer;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;
import log.charter.util.collections.HashSet2;

public class ChartItemsHandler {
	private static <P extends IVirtualPositionWithEnd> void changePositionLength(final ImmutableBeatsMap beats,
			final List<P> allPositions, final Selection<P> selected, final int gridsChange) {
		final IVirtualPositionWithEnd positionWithLength = selected.selectable;

		IVirtualConstantPosition endPosition = positionWithLength.endPosition();
		endPosition = beats.addGrid(endPosition, gridsChange);

		if (selected.id + 1 < allPositions.size()) {
			final P next = allPositions.get(selected.id + 1);
			final IVirtualConstantPosition maxPosition = add(beats, next, new Position(minNoteDistance));
			endPosition = min(IVirtualConstantPosition.comparator(beats), endPosition, maxPosition);
		}

		positionWithLength.endPosition(beats, endPosition);
	}

	private static void changeNoteLength(final ImmutableBeatsMap beats, final List<ChordOrNote> sounds,
			final CommonNote note, final int id, final int gridsChange) {
		if (note.linkNext()) {
			LinkedNotesFixer.fixLinkedNote(note, id, sounds);
			return;
		}

		IVirtualConstantPosition endPosition = new ConstantPosition(note.endPosition());
		endPosition = beats.addGrid(endPosition, gridsChange);
		note.endPosition(endPosition.toPosition(beats).position());

		fixNoteLength(note, id, sounds);
	}

	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public <T extends IVirtualConstantPosition> void delete() {
		boolean nonEmptyFound = false;

		for (final PositionType type : PositionType.values()) {
			if (type == PositionType.NONE || type == PositionType.BEAT) {
				continue;
			}

			final ISelectionAccessor<T> selectedTypeAccessor = selectionManager.accessor(type);
			if (!selectedTypeAccessor.isSelected()) {
				continue;
			}

			final List<Selection<T>> selected = selectedTypeAccessor.getSelected();
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
			case ANCHOR:
				snapPositions(selected.stream().map(selection -> (Anchor) selection.selectable),
						chartData.currentAnchors());
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
		final Vocal from = selected.get(0).selectable;
		final Vocal to = selected.get(selected.size() - 1).selectable;
		snapPositionsWithLength(getFromTo(chartData.currentVocals().vocals, from, to).stream(),
				chartData.currentVocals().vocals);

		reselectAfterSnapping(accessor.type(), selected);
	}

	private <T extends IVirtualPosition> void snapAllOnGuitar(final ISelectionAccessor<T> accessor) {
		undoSystem.addUndo();

		final List<Selection<T>> selected = accessor.getSelected();
		final T from = selected.get(0).selectable;
		final T to = selected.get(selected.size() - 1).selectable;
		final Arrangement arrangement = chartData.currentArrangement();
		final Level level = chartData.currentArrangementLevel();
		final Comparator<IVirtualConstantPosition> comparator = IVirtualConstantPosition.comparator(chartData.beats());

		snapPositions(getFromTo(arrangement.eventPoints, from, to, comparator).stream(), arrangement.eventPoints);
		snapPositions(getFromTo(arrangement.toneChanges, from, to, comparator).stream(), arrangement.toneChanges);
		snapPositions(getFromTo(level.anchors, from, to, comparator).stream(), level.anchors);
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

		for (final Selection<ChordOrNote> selection : selectionManager.getSelectedSounds()) {
			sounds.set(selection.id, mapper.apply(selection.selectable));
		}
	}

	public <P extends IVirtualPositionWithEnd> void changePositionsWithLengthsByGrid(final List<Selection<P>> toChange,
			final List<P> allPositions, final int gridsChange) {
		final ImmutableBeatsMap beats = chartData.beats();
		for (final Selection<P> selected : toChange) {
			changePositionLength(beats, allPositions, selected, gridsChange);
		}
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
