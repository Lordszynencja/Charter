package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;

class VocalsMenuHandler extends CharterMenuHandler {
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;

	public void init(final KeyboardHandler keyboardHandler, final ModeManager modeManager) {
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() == EditMode.VOCALS;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.VOCALS_MENU);
		menu.add(createItem(keyboardHandler, Action.EDIT_VOCALS));
		menu.add(createItem(keyboardHandler, Action.TOGGLE_WORD_PART));
		menu.add(createItem(keyboardHandler, Action.TOGGLE_PHRASE_END));

		return menu;
	}
}
