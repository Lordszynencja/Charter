package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.config.Localization.Label;
import log.charter.services.Action;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class VocalsMenuHandler extends CharterMenuHandler {
	private ModeManager modeManager;

	@Override
	boolean isApplicable() {
		return modeManager.getMode() == EditMode.VOCALS;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.VOCALS_MENU);
		menu.add(createItem(Action.EDIT_VOCALS));
		menu.add(createItem(Action.TOGGLE_WORD_PART));
		menu.add(createItem(Action.TOGGLE_PHRASE_END));

		menu.addSeparator();
		menu.add(createItem(Action.PLACE_LYRIC_FROM_TEXT));

		return menu;
	}
}
