package log.charter.gui.menuHandlers;

import static log.charter.gui.components.utils.ComponentUtils.showPopup;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.swing.JMenu;

import log.charter.CharterMain;
import log.charter.data.config.Localization;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.util.Utils;

class InfoMenuHandler extends CharterMenuHandler {
	private static final String infoText = "Charter\n"//
			+ "Created by Lord Sai and friends from Customs Forge\n\n"//
			+ "Current version: " + CharterMain.VERSION + " - " + CharterMain.VERSION_DATE;

	private static final String librariesUsed = //
			"part of Widgex made by Joseph Fabisevich\n"//
					+ "FLAC_library-Java made by Nayuki";

	private static final String paypalDonationCode = "https://www.paypal.com/donate/?hosted_button_id=YH2SN57E68LK8";

	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;

	@Override
	boolean isApplicable() {
		return true;
	}

	private JMenu prepareLanguagesSubmenu() {
		final JMenu languagesMenu = createMenu(Label.INFO_MENU_LANGUAGE);
		final File languagesFolder = new File(Localization.languagesFolder);
		if (!languagesFolder.isDirectory() || !languagesFolder.exists()) {
			return languagesMenu;
		}

		final String[] languageFiles = languagesFolder.list((dir, name) -> name.endsWith(".txt"));
		if (languageFiles == null) {
			return languagesMenu;
		}

		for (final String fileName : languageFiles) {
			if (fileName == null) {
				continue;
			}

			final String language = fileName.substring(0, fileName.lastIndexOf('.'));
			languagesMenu.add(createItem(language, () -> Localization.changeLanguage(language, charterMenuBar)));
		}

		return languagesMenu;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.INFO_MENU);

		menu.add(createItem(Label.INFO_MENU_VERSION, () -> showPopup(charterFrame, infoText)));
		menu.add(createItem(Label.LICENSES, () -> showPopup(charterFrame, Label.LIBRARIES_USED.format(librariesUsed))));
		menu.add(prepareLanguagesSubmenu());

		menu.addSeparator();
		menu.add(createItem(Label.CONFIGS_AND_LOGS, this::openConfigFolder));

		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
			menu.addSeparator();
			menu.add(createItem(Label.INFO_MENU_DONATION, this::openDonationPage));
		}

		return menu;
	}

	private void openDonationPage() {
		try {
			Desktop.getDesktop().browse(new URI(paypalDonationCode));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void showConfigFolderPosition() {
		showPopup(charterFrame, Label.CONFIGS_AND_LOGS_MESSAGE.format(Utils.defaultConfigDir));
	}

	private void openConfigFolder() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.OPEN)) {
			try {
				Desktop.getDesktop().open(new File(Utils.defaultConfigDir));
			} catch (final IOException e) {
				showConfigFolderPosition();
			}
		} else {
			showConfigFolderPosition();
		}
	}

}
