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

		// addSeparator(menu);
		// final JMenu copyFromMenu = new JMenu("Copy from");

//		for (final InstrumentType type : InstrumentType.sortedValues()) {
//			final JMenu copyFromMenuInstr = new JMenu(type.name);
//			for (int i = 0; i < Instrument.diffNames.length; i++) {
//				final int diff = i;
//				copyFromMenuInstr.add(createItem(Instrument.diffNames[i], e -> handler.copyFrom(type, diff)));
//			}
//			copyFromMenu.add(copyFromMenuInstr);
//		}

		// menu.add(copyFromMenu);

		return menu;
	}

	private void gridOptions() {
		new GridPane(frame, data.songChart.beatsMap);
	}

	private void doubleGridSize() {
		data.songChart.beatsMap.gridSize *= 2;
		Config.lastGridSize = data.songChart.beatsMap.gridSize;
		Config.markChanged();
	}

	private void halveGridSize() {
		if (data.songChart.beatsMap.gridSize % 2 == 0) {
			data.songChart.beatsMap.gridSize /= 2;
			Config.lastGridSize = data.songChart.beatsMap.gridSize;
			Config.markChanged();
		}
	}

	private void snapNotes() {
		// TODO
	}

}
