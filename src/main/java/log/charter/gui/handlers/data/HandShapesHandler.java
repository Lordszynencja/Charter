package log.charter.gui.handlers.data;

import static log.charter.song.notes.IConstantPosition.findFirstIdAfter;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;

import log.charter.data.ChartData;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.HandShapePane;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

public class HandShapesHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void init(final ChartData chartData, final CharterFrame charterFrame,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.chartData = chartData;
		this.charterFrame = charterFrame;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	public void markHandShape() {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final ArrayList2<HandShape> handShapes = chartData.getCurrentArrangementLevel().handShapes;
		final ArrayList2<Selection<ChordOrNote>> selected = selectionAccessor.getSortedSelected();
		final int position = selected.get(0).selectable.position();
		final int endPosition = selected.getLast().selectable.endPosition();

		int deleteFromId = findLastIdBefore(handShapes, position);
		if (deleteFromId == -1) {
			deleteFromId = 0;
		}
		if (handShapes.size() > deleteFromId && handShapes.get(deleteFromId).endPosition() < position) {
			deleteFromId++;
		}

		final int firstIdAfter = findFirstIdAfter(handShapes, endPosition);
		final int deleteToId = firstIdAfter == -1 ? handShapes.size() - 1 : firstIdAfter - 1;
		for (int i = deleteToId; i >= deleteFromId; i--) {
			handShapes.remove(i);
		}

		ChordTemplate chordTemplate = new ChordTemplate();
		if (selected.get(0).selectable.isChord()) {
			chordTemplate = chartData.getCurrentArrangement().chordTemplates
					.get(selected.get(0).selectable.chord.templateId());
		}

		final HandShape handShape = new HandShape(position, endPosition - position);
		handShape.templateId = chartData.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);

		handShapes.add(handShape);
		handShapes.sort(null);
		new HandShapePane(chartData, charterFrame, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}
}
