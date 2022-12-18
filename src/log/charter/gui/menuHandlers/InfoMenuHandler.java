package log.charter.gui.menuHandlers;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import log.charter.gui.CharterFrame;
import log.charter.main.LogCharterRSMain;

class InfoMenuHandler extends CharterMenuHandler {
	private static final String infoText = "Lords of Games Rocksmith Charter\n"//
			+ "Created by Lordszynencja\n"//
			+ "Current version: " + LogCharterRSMain.VERSION + "\n\n"//
			+ "TODO:\n"//
			+ "working Save As...\n"//
			+ "GP import\n"//
			+ "note edit\n"//
			+ "a lot more";

	private CharterFrame frame;

	public void init(final CharterFrame frame) {
		this.frame = frame;
	}

	@Override
	boolean isApplicable() {
		return true;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu("Info");

		menu.add(createItem("Version", () -> JOptionPane.showMessageDialog(frame, infoText)));

		return menu;
	}
}
