package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.CharterContext.Initiable;
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

class EditMenuHandler extends CharterMenuHandler implements Initiable {
	private ActionHandler actionHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

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
		menu.add(new SpecialMenuItem(Label.SONG_OPTIONS, this::songOptions));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.ADD_SILENCE, this::addSilence));
		menu.add(new SpecialMenuItem(Label.ADD_DEFAULT_SILENCE, this::addDefaultSilence));
		menu.add(new SpecialMenuItem(Label.ADD_BEATS_AT_THE_START, this::addBeatsAtTheStart));

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
