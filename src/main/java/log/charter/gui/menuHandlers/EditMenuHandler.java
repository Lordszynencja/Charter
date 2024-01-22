package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.panes.AddDefaultSilencePane;
import log.charter.gui.panes.AddSilencePane;
import log.charter.gui.panes.SongOptionsPane;

class EditMenuHandler extends CharterMenuHandler {
	private CopyManager copyManager;
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private SelectionManager selectionManager;
	private UndoSystem undoSystem;

	void init(final CopyManager copyManager, final ChartData data, final CharterFrame frame,
			final KeyboardHandler keyboardHandler, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.copyManager = copyManager;
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
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
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_SILENCE, null, this::addSilence));
		menu.add(new SpecialMenuItem(Label.EDIT_MENU_ADD_DEFAULT_SILENCE, null, this::addDefaultSilence));

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
}
