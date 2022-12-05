package log.charter.data;

import java.util.LinkedList;

public class UndoSystem {
	private final ChartData data;
	private final LinkedList<UndoState> undo = new LinkedList<>();
	private final LinkedList<UndoState> redo = new LinkedList<>();

	public UndoSystem(final ChartData data) {
		this.data = data;
	}

	public void addUndo() {
		undo.add(new UndoState(data));
		while (undo.size() > 100) {
			undo.removeFirst();
		}
		redo.clear();
	}

	public void clear() {
		undo.clear();
		redo.clear();
	}

	public void redo() {
		if (!redo.isEmpty()) {
			undo.add(redo.removeLast().undo(data));
		}
	}

	public void undo() {
		if (!undo.isEmpty()) {
			redo.add(undo.removeLast().undo(data));
		}
	}
}
