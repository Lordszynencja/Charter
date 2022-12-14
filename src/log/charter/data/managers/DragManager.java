package log.charter.data.managers;

import log.charter.data.PositionWithIdAndType;
import log.charter.data.PositionWithIdAndType.PositionType;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.gui.handlers.MouseButtonPressReleaseHandler.MouseButtonPressData;

public class DragManager {
	private MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler;
	private SelectionManager selectionManager;

	public void init(final MouseButtonPressReleaseHandler mouseButtonPressReleaseHandler,
			final SelectionManager selectionManager) {
		this.mouseButtonPressReleaseHandler = mouseButtonPressReleaseHandler;
		this.selectionManager = selectionManager;
	}

	public PositionWithIdAndType getDragStart() {
		final MouseButtonPressData pressData = mouseButtonPressReleaseHandler.getPressPosition(MouseButton.LEFT_BUTTON);
		if (pressData == null) {
			return null;
		}
		if (pressData.highlight.id == null) {
			return null;
		}

		if (pressData.highlight.type == PositionType.NONE) {
			return null;
		}

		final SelectionAccessor<?> selectionAccessor = selectionManager.getSelectedAccessor(pressData.highlight.type);
		if (selectionAccessor == null) {
			return pressData.highlight;
		}

		if (!selectionAccessor.isSelected()) {
			return pressData.highlight;
		}

		if (selectionAccessor.getSelectedSet().contains(selection -> selection.id == pressData.highlight.id)) {
			return pressData.highlight;
		}

		return null;
	}

	public int getDragDistance() {
		final PositionWithIdAndType dragStart = getDragStart();

		if (dragStart == null) {
			return 0;
		}

		return 0;
	}
}
