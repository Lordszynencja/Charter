package log.charter.services.data;

import static log.charter.util.CollectionUtils.firstAfter;
import static log.charter.util.CollectionUtils.lastBefore;

import java.util.List;

import log.charter.data.ChartData;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.HandShape;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.position.fractional.IConstantFractionalPosition;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songEdits.HandShapePane;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.Selection;
import log.charter.services.data.selection.SelectionManager;

public class HandShapesHandler {
	private ChartData chartData;
	private CharterFrame charterFrame;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	public void markHandShape() {
		final ISelectionAccessor<ChordOrNote> selectionAccessor = selectionManager.accessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final List<HandShape> handShapes = chartData.currentHandShapes();
		final List<Selection<ChordOrNote>> selected = selectionAccessor.getSelected();
		final ChordOrNote firstSelected = selected.get(0).selectable;

		final IConstantFractionalPosition position = firstSelected.toFraction(chartData.beats());
		final IConstantFractionalPosition endPosition = selected.get(selected.size() - 1).selectable.endPosition()
				.toFraction(chartData.beats());

		int deleteFromId = lastBefore(handShapes, position).findId(0);
		if (handShapes.size() > deleteFromId && handShapes.get(deleteFromId).endPosition().compareTo(position) < 0) {
			deleteFromId++;
		}

		final int deleteToId = firstAfter(handShapes, endPosition).findId(handShapes.size()) - 1;
		for (int i = deleteToId; i >= deleteFromId; i--) {
			handShapes.remove(i);
		}

		ChordTemplate chordTemplate = new ChordTemplate();
		if (selected.get(0).selectable.isChord()) {
			chordTemplate = chartData.currentArrangement().chordTemplates
					.get(selected.get(0).selectable.chord().templateId());
		}

		final HandShape handShape = new HandShape(position.fractionalPosition(), endPosition.fractionalPosition());
		handShape.templateId = chartData.currentArrangement().getChordTemplateIdWithSave(chordTemplate);

		handShapes.add(deleteFromId, handShape);
		new HandShapePane(chartData, charterFrame, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}
}
