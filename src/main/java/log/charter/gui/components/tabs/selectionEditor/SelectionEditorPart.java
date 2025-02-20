package log.charter.gui.components.tabs.selectionEditor;

import java.util.List;

import log.charter.data.song.position.virtual.IVirtualPosition;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.services.data.selection.SelectionManager;

public abstract class SelectionEditorPart<T extends IVirtualPosition> {
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	private final PositionType type;
	private boolean undoAdded = false;

	protected SelectionEditorPart(final PositionType type) {
		this.type = type;
	}

	protected void addUndo() {
		if (undoAdded) {
			return;
		}

		undoSystem.addUndo();
		undoAdded = true;
	}

	protected List<T> getItems() {
		return selectionManager.getSelectedElements(type);
	}

	public abstract void addTo(final CurrentSelectionEditor currentSelectionEditor);

	public abstract void hide();

	public void selectionChanged() {
		undoAdded = false;
	}
}
