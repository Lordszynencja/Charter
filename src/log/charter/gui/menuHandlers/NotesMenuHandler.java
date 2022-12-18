package log.charter.gui.menuHandlers;

import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_PERIOD;

import javax.swing.JMenu;

import log.charter.data.ChartData;

class NotesMenuHandler extends CharterMenuHandler {

	private ChartData data;

	public void init(final ChartData data) {
		this.data = data;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu("Notes");

		menu.add(createItem("Snap notes to grid", ctrl('F'), this::snapNotes));
		menu.add(createItem("Double grid size", button(VK_PERIOD), this::doubleGridSize));
		menu.add(createItem("Half grid size", button(VK_COMMA), this::halveGridSize));

		menu.addSeparator();
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

	private void doubleGridSize() {
		data.songChart.beatsMap.gridSize *= 2;
	}

	private void halveGridSize() {
		if (data.songChart.beatsMap.gridSize % 2 == 0) {
			data.songChart.beatsMap.gridSize /= 2;
		}
	}

	private void snapNotes() {
		// TODO
	}

}
