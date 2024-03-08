package log.charter.gui.menuHandlers;

import java.io.File;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import log.charter.data.config.Localization;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.main.CharterMain;

class InfoMenuHandler extends CharterMenuHandler {
	private static final String infoText = "Charter\n"//
			+ "Created by Lord Sai and friends from Customs Forge\n\n"//
			+ "Current version: " + CharterMain.VERSION + " - " + CharterMain.VERSION_DATE;

	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;

	@Override
	boolean isApplicable() {
		return true;
	}

	private JMenu prepareLanguagesSubmenu() {
		final JMenu languagesMenu = createMenu(Label.INFO_MENU_LANGUAGE);
		final File languagesFolder = new File(Localization.languagesFolder);
		if (languagesFolder.isDirectory()) {
			for (final String fileName : languagesFolder.list((dir, name) -> name.endsWith(".txt"))) {
				final String language = fileName.substring(0, fileName.lastIndexOf('.'));
				languagesMenu.add(createItem(language, () -> Localization.changeLanguage(language, charterMenuBar)));
			}
		}

		return languagesMenu;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.INFO_MENU);

		menu.add(createItem(Label.INFO_MENU_VERSION, this::showVersion));
		menu.add(prepareLanguagesSubmenu());

		return menu;
	}

	private void showVersion() {
		JOptionPane.showMessageDialog(charterFrame, infoText);
	}
}
