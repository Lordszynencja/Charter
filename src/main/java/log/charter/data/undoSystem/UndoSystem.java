package log.charter.data.undoSystem;

import java.util.LinkedList;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;

public class UndoSystem {
	public static int nextId = 1;

	private ChartData data;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	private final LinkedList<UndoState> undo = new LinkedList<>();
	private final LinkedList<UndoState> redo = new LinkedList<>();
	private int savePosition = 0;

	public void init(final ChartData data, final ModeManager modeManager, final SelectionManager selectionManager) {
		this.data = data;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
	}

	private UndoState createUndoState() {
		return new BaseUndoState(modeManager, data);
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
		if (data.isEmpty) {
			return;
		}
		if (undo.isEmpty()) {
			return;
		}

		selectionManager.clear();
		savePosition--;
		final UndoState lastUndo = undo.removeLast();
		redo.add(lastUndo.undo(data));
	}

	public void redo() {
		if (data.isEmpty) {
			return;
		}
		if (redo.isEmpty()) {
			return;
		}

		selectionManager.clear();
		savePosition++;
		undo.add(redo.removeLast().undo(data));
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
