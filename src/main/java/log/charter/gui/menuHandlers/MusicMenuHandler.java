package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;

class MusicMenuHandler extends CharterMenuHandler {
	private ChartData data;
	private KeyboardHandler keyboardHandler;

	public void init(final ChartData data, final KeyboardHandler keyboardHandler) {
		this.data = data;
		this.keyboardHandler = keyboardHandler;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.MUSIC_MENU);
		menu.add(createItem(keyboardHandler, Action.TOGGLE_REPEATER));
		menu.add(createItem(keyboardHandler, Action.TOGGLE_REPEAT_START));
		menu.add(createItem(keyboardHandler, Action.TOGGLE_REPEAT_END));

		return menu;
	}
}
