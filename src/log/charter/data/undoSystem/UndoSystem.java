package log.charter.data.undoSystem;

import java.util.LinkedList;

import log.charter.data.ChartData;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;

public class UndoSystem {
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

	public void addUndo() {
		savePosition++;

		undo.add(createUndoState());
		while (undo.size() > 100) {
			undo.removeFirst();
		}

		redo.clear();
	}

	public void undo() {
		if (undo.isEmpty()) {
			return;
		}

		selectionManager.clear();
		savePosition--;
		final UndoState lastUndo = undo.removeLast();
		redo.add(lastUndo.undo(data));
	}

	public void redo() {
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
}
