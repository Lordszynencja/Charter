package log.charter.services.mouseAndKeyboard;

import static log.charter.util.CollectionUtils.contains;

import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.services.data.selection.ISelectionAccessor;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButton;
import log.charter.services.mouseAndKeyboard.MouseButtonPressReleaseHandler.MouseButtonPressData;

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
		if (pressData == null || pressData.highlight.id == null || pressData.highlight.type == PositionType.NONE) {
			return null;
		}

		final ISelectionAccessor<?> selectionAccessor = selectionManager.accessor(pressData.highlight.type);
		if (selectionAccessor == null || !selectionAccessor.isSelected()//
				|| contains(selectionAccessor.getSelected2(), s -> s.id == pressData.highlight.id)) {
			return pressData.highlight;
		}

		return null;
	}
}
