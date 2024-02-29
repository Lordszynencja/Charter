package log.charter.gui.handlers.data;

import static java.util.Arrays.asList;
import static log.charter.song.notes.IConstantPosition.getFromTo;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
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
	private ChartData data;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ArrangementFixer arrangementFixer, final ChartData data, final ModeManager modeManager,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.arrangementFixer = arrangementFixer;
		this.data = data;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public void delete() {
		boolean undoAdded = false;

		for (final PositionType type : PositionType.values()) {
			if (type == PositionType.NONE
					|| (type == PositionType.BEAT && modeManager.getMode() != EditMode.TEMPO_MAP)) {
				continue;
			}

			final SelectionAccessor<Position> selectedTypeAccessor = selectionManager.getSelectedAccessor(type);
			if (selectedTypeAccessor.isSelected()) {
				if (!undoAdded) {
					undoSystem.addUndo();
					undoAdded = true;
				}

				final ArrayList2<Selection<Position>> selected = selectedTypeAccessor.getSortedSelected();
				final ArrayList2<IPosition> positions = type.getPositions(data);
				for (int i = selected.size() - 1; i >= 0; i--) {
					positions.remove(selected.get(i).id);
				}

				if (type == PositionType.BEAT) {
					data.songChart.beatsMap.fixFirstBeatInMeasures();
				}
				if (type == PositionType.TONE_CHANGE) {
					data.getCurrentArrangement().tones = data.getCurrentArrangement().toneChanges.stream()//
							.map(toneChange -> toneChange.toneName)//
							.collect(Collectors.toCollection(HashSet2::new));
				}

			}
		}

		selectionManager.clear();
	}

	private void snapPositions(final Collection<? extends IPosition> positions) {
		for (final IPosition position : positions) {
			final int newPosition = data.songChart.beatsMap.getPositionFromGridClosestTo(position.position());
			position.position(newPosition);
		}
	}

	private void snapNotePositions(final Collection<ChordOrNote> positions) {
		snapPositions(positions);

		final ArrayList2<ChordOrNote> sounds = data.getCurrentArrangementLevel().sounds;
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
						data.getCurrentArrangementLevel().handShapes);
				break;
			case VOCAL:
				snapPositionsWithLength(selected.map(selection -> (Vocal) selection.selectable),
						data.songChart.vocals.vocals);
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

			snapPositionsWithLength(getFromTo(data.songChart.vocals.vocals, from, to), data.songChart.vocals.vocals);

			reselectAfterSnapping(accessor.type, selected);
			return;
		}

		if (modeManager.getMode() == EditMode.GUITAR) {
			undoSystem.addUndo();

			final Arrangement arrangement = data.getCurrentArrangement();
			final Level level = data.getCurrentArrangementLevel();
			snapPositions(getFromTo(arrangement.eventPoints, from, to));
			snapPositions(getFromTo(arrangement.toneChanges, from, to));
			snapPositions(getFromTo(level.anchors, from, to));
			snapNotePositions(getFromTo(level.sounds, from, to));
			snapPositionsWithLength(getFromTo(level.handShapes, from, to), level.handShapes);

			reselectAfterSnapping(accessor.type, selected);
		}
	}
}
