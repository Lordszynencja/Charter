package log.charter.data.undoSystem;

import java.util.LinkedList;

import log.charter.data.ChartData;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.ModeManager;

public class UndoSystem {
	public static int nextId = 1;

	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	private final LinkedList<UndoState> undo = new LinkedList<>();
	private final LinkedList<UndoState> redo = new LinkedList<>();
	private int savePosition = 0;

	private UndoState createUndoState() {
		return new BaseUndoState(chartTimeHandler, modeManager, chartData);
	}

	public void addUndo(final UndoState undoState) {
		savePosition++;

		undo.add(undoState);
		while (undo.size() > 100) {
			undo.removeFirst();
		}

		redo.clear();
	}

	public void addUndo() {
		addUndo(createUndoState());
	}

	public void undo() {
		if (chartData.isEmpty) {
			return;
		}
		if (undo.isEmpty()) {
			return;
		}

		selectionManager.clear();
		savePosition--;
		final UndoState lastUndo = undo.removeLast();
		redo.add(lastUndo.undo(chartData, chartTimeHandler));
	}

	public void redo() {
		if (chartData.isEmpty) {
			return;
		}
		if (redo.isEmpty()) {
			return;
		}

		selectionManager.clear();
		savePosition++;
		undo.add(redo.removeLast().undo(chartData, chartTimeHandler));
	}

	public void clear() {
		undo.clear();
		redo.clear();
		savePosition = 0;
	}

	public void onSave() {
		savePosition = 0;
	}

	public boolean isSaved() {
		return savePosition == 0;
	}

	public int getLastUndoId() {
		return undo.isEmpty() ? -1 : undo.getLast().id;
	}

	public void removeRedo() {
		redo.clear();
	}
}
