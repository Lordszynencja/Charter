package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.panes.songEdits.AddBeatsAtTheStartPane;
import log.charter.gui.panes.songEdits.AddDefaultSilencePane;
import log.charter.gui.panes.songEdits.AddSilencePane;
import log.charter.gui.panes.songSettings.SongOptionsPane;

class EditMenuHandler extends CharterMenuHandler {
	private CopyManager copyManager;
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	void init(final CopyManager copyManager, final ChartData data, final CharterFrame frame,
			final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final SelectionManager selectionManager, final UndoSystem undoSystem) {
		this.copyManager = copyManager;
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.undoSystem = undoSystem;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.EDIT_MENU.label());

		menu.add(new SpecialMenuItem(Label.EDIT_MENU_UNDO, "Ctrl-Z", undoSystem::undo));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_REDO, "Ctrl-R", undoSystem::redo));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_SELECT_ALL, "Ctrl-A", selectionManager::selectAllNotes));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_DELETE, "Del", keyboardHandler::delete));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_COPY, "Ctrl-C", copyManager::copy));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_PASTE, "Ctrl-V", copyManager::paste));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_SPECIAL_PASTE, "Ctrl-Shift-V", copyManager::specialPaste));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_SONG_OPTIONS, null, this::songOptions));

		if (modeManager.getMode() == EditMode.TEMPO_MAP) {
			menu.addSeparator();
			menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_SILENCE, null, this::addSilence));
			menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_DEFAULT_SILENCE, null, this::addDefaultSilence));
			menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_BEATS_AT_THE_START, null, this::addBeatsAtTheStart));
		}

		return menu;
	}

	private void songOptions() {
		new SongOptionsPane(frame, data);
	}

	private void addSilence() {
		new AddSilencePane(frame, data);
	}

	private void addDefaultSilence() {
		new AddDefaultSilencePane(frame, data);
	}

	private void addBeatsAtTheStart() {
		new AddBeatsAtTheStartPane(frame, data, undoSystem);
	}
}
