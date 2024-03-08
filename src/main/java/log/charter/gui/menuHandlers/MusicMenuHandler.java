package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;

class MusicMenuHandler extends CharterMenuHandler implements Initiable {
	private ActionHandler actionHandler;
	private ModeManager modeManager;

	@Override
	public void init() {
		super.init(actionHandler);
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() != EditMode.EMPTY;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.MUSIC_MENU);
		menu.add(createItem(Action.TOGGLE_REPEATER));
		menu.add(createItem(Action.TOGGLE_REPEAT_START));
		menu.add(createItem(Action.TOGGLE_REPEAT_END));

		return menu;
	}
}
