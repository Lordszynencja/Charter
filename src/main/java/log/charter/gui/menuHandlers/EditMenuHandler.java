package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.config.values.DebugConfig;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.SpecialMenuItem;
import log.charter.gui.panes.songEdits.AddBeatsAtTheStartPane;
import log.charter.gui.panes.songEdits.AddDefaultSilencePane;
import log.charter.gui.panes.songEdits.AddSilenceAtTheEndPane;
import log.charter.gui.panes.songEdits.AddSilenceInTheBeginningPane;
import log.charter.gui.panes.songEdits.ChangeSongPitchPane;
import log.charter.services.Action;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class EditMenuHandler extends CharterMenuHandler {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

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
		menu.add(createItem(Action.DELETE));
		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.add(createItem(Action.DELETE_RELATED));
		}
		menu.add(createItem(Action.COPY));
		menu.add(createItem(Action.PASTE));
		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.add(createItem(Action.SPECIAL_PASTE));
		}
	}

	private void addBookmarksSubmenu(final JMenu menu) {
		final JMenu bookmarksSubmenu = createMenu(Label.BOOKMARKS_MENU);
		for (int i = 0; i < 10; i++) {
			bookmarksSubmenu.add(createItem(Action.valueOf("MARK_BOOKMARK_" + i)));
		}
		bookmarksSubmenu.addSeparator();
		for (int i = 0; i < 10; i++) {
			bookmarksSubmenu.add(createItem(Action.valueOf("MOVE_TO_BOOKMARK_" + i)));
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
		menu.add(new SpecialMenuItem(Label.ADD_SILENCE_IN_THE_BEGINNING, this::addSilenceInTheBeginning));
		menu.add(new SpecialMenuItem(Label.ADD_DEFAULT_START_SILENCE, this::addDefaultSilence));
		menu.add(new SpecialMenuItem(Label.ADD_BEATS_AT_THE_START, this::addBeatsAtTheStart));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.ADD_SILENCE_AT_THE_END, this::addSilenceIAtTheEnd));

		if (DebugConfig.enablePitchShifting) {
			menu.addSeparator();
			menu.add(new SpecialMenuItem(Label.CHANGE_SONG_PITCH, this::changeSongPitch));
		}

		menu.addSeparator();
		addBookmarksSubmenu(menu);

		if (modeManager.getMode() == EditMode.TEMPO_MAP) {
			menu.add(createDisabledItem(Action.TOGGLE_ANCHOR));
			menu.add(createDisabledItem(Action.BEAT_ADD));
			menu.add(createDisabledItem(Action.BEAT_REMOVE));
			menu.add(createDisabledItem(Action.BPM_DOUBLE));
			menu.add(createDisabledItem(Action.BPM_HALVE));
		}

		return menu;
	}

	private void addSilenceInTheBeginning() {
		new AddSilenceInTheBeginningPane(charterFrame, chartTimeHandler, chartData, projectAudioHandler);
	}

	private void addDefaultSilence() {
		new AddDefaultSilencePane(charterFrame, chartTimeHandler, chartData, projectAudioHandler);
	}

	private void addBeatsAtTheStart() {
		new AddBeatsAtTheStartPane(charterFrame, chartData, undoSystem);
	}

	private void addSilenceIAtTheEnd() {
		new AddSilenceAtTheEndPane(charterFrame, projectAudioHandler);
	}

	private void changeSongPitch() {
		new ChangeSongPitchPane(charterFrame, projectAudioHandler);
	}
}
