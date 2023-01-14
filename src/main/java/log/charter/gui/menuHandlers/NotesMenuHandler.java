package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.panes.GridPane;

class NotesMenuHandler extends CharterMenuHandler {

	private CharterFrame frame;
	private ChartData data;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;

	public void init(final CharterFrame frame, final ChartData data, final KeyboardHandler keyboardHandler,
			final ModeManager modeManager) {
		this.frame = frame;
		this.data = data;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty && modeManager.editMode != EditMode.TEMPO_MAP;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.NOTES_MENU.label());

		menu.add(new SpecialMenuItem(Label.EDIT_MENU_GRID_OPTIONS, "G", this::gridOptions));
		// menu.add(new SpecialMenuItem(Label.NOTES_MENU_SNAP, "Ctrl-G", null));
		menu.add(new SpecialMenuItem(Label.NOTES_MENU_HALVE_GRID, ",", keyboardHandler::halveGridSize));
		menu.add(new SpecialMenuItem(Label.NOTES_MENU_DOUBLE_GRID, ".", keyboardHandler::doubleGridSize));

		return menu;
	}

	private void gridOptions() {
		new GridPane(frame);
	}
}
