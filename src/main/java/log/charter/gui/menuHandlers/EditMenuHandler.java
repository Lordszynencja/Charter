package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.DebugConfig;
import log.charter.gui.panes.songEdits.ChangeSongPitchPane;
import log.charter.services.Action;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class EditMenuHandler extends CharterMenuHandler {
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;

	@Override
	boolean isApplicable() {
		return modeManager.getMode() != EditMode.EMPTY;
	}

	private void addSelectOptionsIfNeeded(final JMenu menu) {
		if (modeManager.getMode() != EditMode.VOCALS && modeManager.getMode() != EditMode.GUITAR) {
			return;
		}

		menu.addSeparator();
		menu.add(createItem(Action.SELECT_ALL));
		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.add(createItem(Action.SELECT_LIKE));
		}
		menu.add(createItem(Action.DELETE));
		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.add(createItem(Action.DELETE_RELATED));
		}
		menu.add(createItem(Action.COPY));
		menu.add(createItem(Action.CUT));
		menu.add(createItem(Action.PASTE));
		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.add(createItem(Action.SPECIAL_PASTE));
		}
	}

	private void addBookmarksSubmenu(final JMenu menu) {
		final JMenu bookmarksSubmenu = createMenu(Label.BOOKMARKS_MENU);
		for (int i = 0; i < 10; i++) {
			bookmarksSubmenu.add(createItem(Action.valueOf("BOOKMARK_" + i)));
		}
		menu.add(bookmarksSubmenu);
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.EDIT_MENU);
		menu.add(createItem(Action.UNDO));
		menu.add(createItem(Action.REDO));

		addSelectOptionsIfNeeded(menu);

		menu.addSeparator();
		menu.add(createItem(Action.CHANGE_GRID));
		if (modeManager.modeIs(EditMode.GUITAR, EditMode.VOCALS)) {
			menu.add(createItem(Action.DECREASE_LENGTH));
			menu.add(createItem(Action.INCREASE_LENGTH));
			menu.add(createItem(Action.DECREASE_LENGTH_FAST));
			menu.add(createItem(Action.INCREASE_LENGTH_FAST));
		}

		if (DebugConfig.enablePitchShifting) {
			menu.addSeparator();
			menu.add(createItem(Label.CHANGE_SONG_PITCH, this::changeSongPitch));
		}

		menu.addSeparator();
		addBookmarksSubmenu(menu);

		menu.addSeparator();
		menu.add(createItem(Action.ZOOM_IN));
		menu.add(createItem(Action.ZOOM_IN_FAST));
		menu.add(createItem(Action.ZOOM_OUT));
		menu.add(createItem(Action.ZOOM_OUT_FAST));

		return menu;
	}

	private void changeSongPitch() {
		new ChangeSongPitchPane(charterFrame, projectAudioHandler);
	}
}
