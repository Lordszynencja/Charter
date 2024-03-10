package log.charter.services.data;

import static java.util.Arrays.asList;
import static log.charter.song.notes.IConstantPosition.getFromTo;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.data.ChartData;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.data.fixers.ArrangementFixer;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.song.Arrangement;
import log.charter.song.HandShape;
import log.charter.song.Level;
import log.charter.song.notes.ChordOrNote;
import log.charter.song.notes.IPosition;
import log.charter.song.notes.IPositionWithLength;
import log.charter.song.notes.Position;
import log.charter.song.vocals.Vocal;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashSet2;

public class ChartItemsHandler {
	private ArrangementFixer arrangementFixer;
	private ChartData chartData;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void delete() {
		boolean nonEmptyFound = false;

		for (final PositionType type : PositionType.values()) {
			if (type == PositionType.NONE || type == PositionType.BEAT) {
				continue;
			}

			final SelectionAccessor<Position> selectedTypeAccessor = selectionManager.getSelectedAccessor(type);
			if (!selectedTypeAccessor.isSelected()) {
				continue;
			}

			final ArrayList2<Selection<Position>> selected = selectedTypeAccessor.getSortedSelected();
			if (!nonEmptyFound) {
				undoSystem.addUndo();
				selectionManager.clear();
				nonEmptyFound = true;
			}

			delete(type, selected.map(selection -> selection.id));
		}
	}

	public void delete(final PositionType type, final List<Integer> idsToDelete) {
		if (type == PositionType.NONE || type == PositionType.BEAT//
				|| idsToDelete.isEmpty()) {
			return;
		}

		idsToDelete.sort((a, b) -> -Integer.compare(a, b));

		final ArrayList2<IPosition> positions = type.getPositions(chartData);
		idsToDelete.forEach(id -> positions.remove((int) id));

		if (type == PositionType.TONE_CHANGE) {
			chartData.getCurrentArrangement().tones = chartData.getCurrentArrangement().toneChanges.stream()//
					.map(toneChange -> toneChange.toneName)//
					.collect(Collectors.toCollection(HashSet2::new));
		}
	}

	private void snapPositions(final Collection<? extends IPosition> positions) {
		for (final IPosition position : positions) {
			final int newPosition = chartData.songChart.beatsMap.getPositionFromGridClosestTo(position.position());
			position.position(newPosition);
		}
	}

	private void snapNotePositions(final Collection<ChordOrNote> positions) {
		snapPositions(positions);

		final ArrayList2<ChordOrNote> sounds = chartData.getCurrentArrangementLevel().sounds;
		for (int i = 1; i < sounds.size(); i++) {
			while (i < sounds.size() && sounds.get(i).position() == sounds.get(i - 1).position()) {
				sounds.remove(i);
			}
		}

		arrangementFixer.fixNoteLengths(sounds);
	}

	private <T extends IPositionWithLength> void snapPositionsWithLength(final Collection<T> positions,
			final ArrayList2<T> allPositions) {
		snapPositions(positions);
		arrangementFixer.fixLengths(allPositions);
	}

	private void reselectAfterSnapping(final PositionType type, final Collection<Selection<IPosition>> selected) {
		final Set<Integer> selectedPositions = selected.stream()//
				.map(selection -> selection.selectable.position())//
				.collect(Collectors.toSet());

		selectionManager.clear();
		selectionManager.addSelectionForPositions(type, selectedPositions);
	}

	public void snapSelected() {
		final SelectionAccessor<IPosition> accessor = selectionManager.getCurrentlySelectedAccessor();
		if (!accessor.isSelected() || !asList(PositionType.EVENT_POINT, PositionType.TONE_CHANGE, PositionType.ANCHOR,
				PositionType.GUITAR_NOTE, PositionType.HAND_SHAPE, PositionType.VOCAL).contains(accessor.type)) {
			return;
		}

		undoSystem.addUndo();

		final HashSet2<Selection<IPosition>> selected = accessor.getSelectedSet();

		switch (accessor.type) {
			case EVENT_POINT:
			case ANCHOR:
			case TONE_CHANGE:
				snapPositions(selected.map(selection -> selection.selectable));
				break;
			case GUITAR_NOTE:
				snapNotePositions(selected.map(selection -> (ChordOrNote) selection.selectable));
				break;
			case HAND_SHAPE:
				snapPositionsWithLength(selected.map(selection -> (HandShape) selection.selectable),
						chartData.getCurrentArrangementLevel().handShapes);
				break;
			case VOCAL:
				snapPositionsWithLength(selected.map(selection -> (Vocal) selection.selectable),
						chartData.songChart.vocals.vocals);
				break;
			default:
				break;
		}

		reselectAfterSnapping(accessor.type, selected);
	}

	public void snapAll() {
		final SelectionAccessor<IPosition> accessor = selectionManager.getCurrentlySelectedAccessor();
		if (!accessor.isSelected()) {
			return;
		}

		final ArrayList2<Selection<IPosition>> selected = accessor.getSortedSelected();
		final int from = selected.get(0).selectable.position();
		final int to = selected.getLast().selectable.position();

		if (modeManager.getMode() == EditMode.TEMPO_MAP) {
			return;
		}

		if (modeManager.getMode() == EditMode.VOCALS) {
			undoSystem.addUndo();

			snapPositionsWithLength(getFromTo(chartData.songChart.vocals.vocals, from, to),
					chartData.songChart.vocals.vocals);

			reselectAfterSnapping(accessor.type, selected);
			return;
		}

		if (modeManager.getMode() == EditMode.GUITAR) {
			undoSystem.addUndo();

			final Arrangement arrangement = chartData.getCurrentArrangement();
			final Level level = chartData.getCurrentArrangementLevel();
			snapPositions(getFromTo(arrangement.eventPoints, from, to));
			snapPositions(getFromTo(arrangement.toneChanges, from, to));
			snapPositions(getFromTo(level.anchors, from, to));
			snapNotePositions(getFromTo(level.sounds, from, to));
			snapPositionsWithLength(getFromTo(level.handShapes, from, to), level.handShapes);

			reselectAfterSnapping(accessor.type, selected);
		}
	}

	public void mapSounds(final Function<ChordOrNote, ChordOrNote> mapper) {
		final ArrayList2<ChordOrNote> sounds = chartData.getCurrentArrangementLevel().sounds;
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		final HashSet2<Selection<ChordOrNote>> selected = selectionAccessor.getSelectedSet();

		for (final Selection<ChordOrNote> selection : selected) {
			sounds.set(selection.id, mapper.apply(selection.selectable));
		}

		selectionManager.refresh(PositionType.GUITAR_NOTE);
	}
}
