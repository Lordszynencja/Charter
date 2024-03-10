package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.config.Localization.Label;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

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
