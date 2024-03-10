package log.charter.services.data;

import static log.charter.data.song.notes.IConstantPosition.findFirstIdAfter;
import static log.charter.data.song.notes.IConstantPosition.findLastIdBefore;

import log.charter.data.ChartData;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.HandShapePane;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.util.CollectionUtils.ArrayList2;

public class HandShapesHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

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
					.get(selected.get(0).selectable.chord().templateId());
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
