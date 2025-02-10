package log.charter.gui.menuHandlers;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.SpecialMenuItem;
import log.charter.gui.panes.songEdits.AddBeatsAtTheStartPane;
import log.charter.gui.panes.songEdits.AddDefaultSilencePane;
import log.charter.gui.panes.songEdits.AddSilencePane;
import log.charter.gui.panes.songSettings.SongOptionsPane;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class EditMenuHandler extends CharterMenuHandler implements Initiable {
	private ActionHandler actionHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

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
		final JMenu menu = createMenu(Label.EDIT_MENU);
		menu.add(createItem(Action.UNDO));
		menu.add(createItem(Action.REDO));

		if (modeManager.getMode() == EditMode.VOCALS || modeManager.getMode() == EditMode.GUITAR) {
			menu.addSeparator();
			menu.add(createItem(Action.SELECT_ALL_NOTES));
			menu.add(createItem(Action.DELETE));
			menu.add(createItem(Action.COPY));
			menu.add(createItem(Action.PASTE));
			if (modeManager.getMode() == EditMode.GUITAR) {
				menu.add(createItem(Action.SPECIAL_PASTE));
			}
		}

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.SONG_OPTIONS, this::songOptions));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.ADD_SILENCE, this::addSilence));
		menu.add(new SpecialMenuItem(Label.ADD_DEFAULT_SILENCE, this::addDefaultSilence));
		menu.add(new SpecialMenuItem(Label.ADD_BEATS_AT_THE_START, this::addBeatsAtTheStart));

		menu.addSeparator();
		final JMenu bookmarksSubmenu = createMenu(Label.BOOKMARKS_MENU);
		for (int i = 0; i < 10; i++) {
			bookmarksSubmenu.add(createItem(Action.valueOf("MARK_BOOKMARK_" + i)));
		}
		bookmarksSubmenu.addSeparator();
		for (int i = 0; i < 10; i++) {
			bookmarksSubmenu.add(createItem(Action.valueOf("MOVE_TO_BOOKMARK_" + i)));
		}
		menu.add(bookmarksSubmenu);

		if (modeManager.getMode() == EditMode.TEMPO_MAP) {
			final JMenuItem anchorItem = createItem(Action.TOGGLE_ANCHOR);
			anchorItem.setEnabled(false);
			menu.add(anchorItem);
		}

		return menu;
	}

	private void songOptions() {
		new SongOptionsPane(charterFrame, chartData);
	}

	private void addSilence() {
		new AddSilencePane(charterFrame, chartTimeHandler, chartData, projectAudioHandler);
	}

	private void addDefaultSilence() {
		new AddDefaultSilencePane(charterFrame, chartTimeHandler, chartData, projectAudioHandler);
	}

	private void addBeatsAtTheStart() {
		new AddBeatsAtTheStartPane(charterFrame, chartData, undoSystem);
	}
}
