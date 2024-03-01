package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.SpecialMenuItem;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.panes.songEdits.AddBeatsAtTheStartPane;
import log.charter.gui.panes.songEdits.AddDefaultSilencePane;
import log.charter.gui.panes.songEdits.AddSilencePane;
import log.charter.gui.panes.songSettings.SongOptionsPane;

class EditMenuHandler extends CharterMenuHandler {
	private ChartTimeHandler chartTimeHandler;
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

	void init(final ChartTimeHandler chartTimeHandler, final ChartData data, final CharterFrame frame,
			final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final ProjectAudioHandler projectAudioHandler, final UndoSystem undoSystem) {
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
		this.projectAudioHandler = projectAudioHandler;
		this.undoSystem = undoSystem;
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() != EditMode.EMPTY;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.EDIT_MENU);
		menu.add(createItem(keyboardHandler, Action.UNDO));
		menu.add(createItem(keyboardHandler, Action.REDO));

		menu.addSeparator();
		if (modeManager.getMode() == EditMode.VOCALS || modeManager.getMode() == EditMode.GUITAR) {
			menu.add(createItem(keyboardHandler, Action.SELECT_ALL_NOTES));
			menu.add(createItem(keyboardHandler, Action.DELETE));
			menu.add(createItem(keyboardHandler, Action.COPY));
			menu.add(createItem(keyboardHandler, Action.PASTE));
			if (modeManager.getMode() == EditMode.GUITAR) {
				menu.add(createItem(keyboardHandler, Action.SPECIAL_PASTE));
			}
		}

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_SONG_OPTIONS, this::songOptions));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_SILENCE, this::addSilence));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_DEFAULT_SILENCE, this::addDefaultSilence));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_BEATS_AT_THE_START, this::addBeatsAtTheStart));

		return menu;
	}

	private void songOptions() {
		new SongOptionsPane(frame, data);
	}

	private void addSilence() {
		new AddSilencePane(frame, chartTimeHandler, data, projectAudioHandler);
	}

	private void addDefaultSilence() {
		new AddDefaultSilencePane(frame, chartTimeHandler, data, projectAudioHandler);
	}

	private void addBeatsAtTheStart() {
		new AddBeatsAtTheStartPane(frame, data, undoSystem);
	}
}
