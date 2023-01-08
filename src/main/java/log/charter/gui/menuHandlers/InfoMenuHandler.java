package log.charter.gui.menuHandlers;

import java.io.File;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import log.charter.data.config.Localization;
import log.charter.data.config.Localization.Label;
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
	private CharterMenuBar charterMenuBar;

	public void init(final CharterFrame frame, final CharterMenuBar charterMenuBar) {
		this.frame = frame;
		this.charterMenuBar = charterMenuBar;
	}

	@Override
	boolean isApplicable() {
		return true;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu languageMenu = new JMenu(Label.INFO_MENU_LANGUAGE.label());
		final File languagesFolder = new File(Localization.languagesFolder);
		if (languagesFolder.isDirectory()) {
			for (final String fileName : languagesFolder.list((dir, name) -> name.endsWith(".txt"))) {
				final String language = fileName.substring(0, fileName.lastIndexOf('.'));
				languageMenu.add(createItem(language, () -> Localization.changeLanguage(language, charterMenuBar)));
			}
		}

		final JMenu menu = new JMenu(Label.INFO_MENU.label());

		menu.add(createItem(Label.INFO_MENU_VERSION, this::showVersion));
		menu.add(languageMenu);

		return menu;
	}

	private void showVersion() {
		JOptionPane.showMessageDialog(frame, infoText);
	}
}
