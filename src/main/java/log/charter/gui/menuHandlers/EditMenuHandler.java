package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.SpecialMenuItem;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.panes.songEdits.AddBeatsAtTheStartPane;
import log.charter.gui.panes.songEdits.AddDefaultSilencePane;
import log.charter.gui.panes.songEdits.AddSilencePane;
import log.charter.gui.panes.songSettings.SongOptionsPane;

class EditMenuHandler extends CharterMenuHandler {
	private ChartTimeHandler chartTimeHandler;
	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

	void init(final ActionHandler actionHandler, final ChartTimeHandler chartTimeHandler, final ChartData data,
			final CharterFrame frame, final ModeManager modeManager, final ProjectAudioHandler projectAudioHandler,
			final UndoSystem undoSystem) {
		super.init(actionHandler);
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.frame = frame;
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
		menu.add(createItem(Action.UNDO));
		menu.add(createItem(Action.REDO));

		menu.addSeparator();
		if (modeManager.getMode() == EditMode.VOCALS || modeManager.getMode() == EditMode.GUITAR) {
			menu.add(createItem(Action.SELECT_ALL_NOTES));
			menu.add(createItem(Action.DELETE));
			menu.add(createItem(Action.COPY));
			menu.add(createItem(Action.PASTE));
			if (modeManager.getMode() == EditMode.GUITAR) {
				menu.add(createItem(Action.SPECIAL_PASTE));
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
