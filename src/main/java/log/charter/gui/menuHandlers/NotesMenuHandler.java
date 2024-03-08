package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;

class NotesMenuHandler extends CharterMenuHandler implements Initiable {
	private ActionHandler actionHandler;
	private ModeManager modeManager;

	public void init() {
		super.init(actionHandler);
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() != EditMode.EMPTY && modeManager.getMode() != EditMode.TEMPO_MAP;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.NOTES_MENU);
		menu.add(createItem(Action.SNAP_SELECTED));
		menu.add(createItem(Action.SNAP_ALL));
		menu.add(createItem(Action.DOUBLE_GRID));
		menu.add(createItem(Action.HALVE_GRID));

		menu.addSeparator();
		menu.add(createItem(Action.PREVIOUS_ITEM));
		menu.add(createItem(Action.PREVIOUS_GRID));
		menu.add(createItem(Action.PREVIOUS_BEAT));
		menu.add(createItem(Action.NEXT_ITEM));
		menu.add(createItem(Action.NEXT_GRID));
		menu.add(createItem(Action.NEXT_BEAT));

		menu.addSeparator();
		menu.add(createItem(Action.MOVE_TO_START));
		menu.add(createItem(Action.MOVE_TO_END));
		menu.add(createItem(Action.MOVE_TO_FIRST_ITEM));
		menu.add(createItem(Action.MOVE_TO_LAST_ITEM));

		return menu;
	}
}
