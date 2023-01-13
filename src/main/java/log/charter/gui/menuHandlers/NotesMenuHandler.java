package log.charter.gui.menuHandlers;

import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_PERIOD;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.GridPane;

class NotesMenuHandler extends CharterMenuHandler {

	private CharterFrame frame;
	private ChartData data;
	private ModeManager modeManager;

	public void init(final CharterFrame frame, final ChartData data, final ModeManager modeManager) {
		this.frame = frame;
		this.data = data;
		this.modeManager = modeManager;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty && modeManager.editMode != EditMode.TEMPO_MAP;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.NOTES_MENU.label());

		menu.add(createItem(Label.EDIT_MENU_GRID_OPTIONS, button('G'), this::gridOptions));
		menu.add(createItem(Label.NOTES_MENU_SNAP, ctrl('G'), this::snapNotes));
		menu.add(createItem(Label.NOTES_MENU_DOUBLE_GRID, button(VK_PERIOD), this::doubleGridSize));
		menu.add(createItem(Label.NOTES_MENU_HALVE_GRID, button(VK_COMMA), this::halveGridSize));

		return menu;
	}

	private void gridOptions() {
		new GridPane(frame);
	}

	private void doubleGridSize() {
		if (Config.gridSize <= 512) {
			Config.gridSize *= 2;
			Config.markChanged();
		}
	}

	private void halveGridSize() {
		if (Config.gridSize % 2 == 0) {
			Config.gridSize /= 2;
			Config.markChanged();
		}
	}

	private void snapNotes() {
		// TODO
	}

}
