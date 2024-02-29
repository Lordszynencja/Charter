package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;

class NotesMenuHandler extends CharterMenuHandler {
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;

	public void init(final KeyboardHandler keyboardHandler, final ModeManager modeManager) {
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() != EditMode.EMPTY && modeManager.getMode() != EditMode.TEMPO_MAP;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.NOTES_MENU.label());
		menu.add(createItem(keyboardHandler, Action.SNAP_SELECTED));
		menu.add(createItem(keyboardHandler, Action.SNAP_ALL));
		menu.add(createItem(keyboardHandler, Action.DOUBLE_GRID));
		menu.add(createItem(keyboardHandler, Action.HALVE_GRID));

		menu.addSeparator();
		menu.add(createItem(keyboardHandler, Action.PREVIOUS_ITEM));
		menu.add(createItem(keyboardHandler, Action.PREVIOUS_GRID));
		menu.add(createItem(keyboardHandler, Action.PREVIOUS_BEAT));
		menu.add(createItem(keyboardHandler, Action.NEXT_ITEM));
		menu.add(createItem(keyboardHandler, Action.NEXT_GRID));
		menu.add(createItem(keyboardHandler, Action.NEXT_BEAT));

		menu.addSeparator();
		menu.add(createItem(keyboardHandler, Action.MOVE_TO_START));
		menu.add(createItem(keyboardHandler, Action.MOVE_TO_END));
		menu.add(createItem(keyboardHandler, Action.MOVE_TO_FIRST_ITEM));
		menu.add(createItem(keyboardHandler, Action.MOVE_TO_LAST_ITEM));

		return menu;
	}
}
