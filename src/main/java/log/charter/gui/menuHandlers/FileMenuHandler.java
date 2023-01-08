package log.charter.gui.menuHandlers;

import static java.awt.event.KeyEvent.VK_ESCAPE;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.panes.ConfigPane;

class FileMenuHandler extends CharterMenuHandler {
	private ChartData data;
	private CharterFrame frame;
	private SongFileHandler songFileHandler;

	public void init(final ChartData data, final CharterFrame frame, final SongFileHandler songFileHandler) {
		this.data = data;
		this.frame = frame;
		this.songFileHandler = songFileHandler;
	}

	@Override
	boolean isApplicable() {
		return true;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.FILE_MENU.label());
		menu.add(createItem(Label.FILE_MENU_NEW, ctrl('N'), songFileHandler::newSong));
		menu.add(createItem(Label.FILE_MENU_OPEN, ctrl('O'), songFileHandler::open));
		menu.add(createItem(Label.FILE_MENU_OPEN_RS, songFileHandler::openSongWithImportFromArrangementXML));
		menu.add(createItem(Label.FILE_MENU_OPEN_AUDIO, songFileHandler::openAudioFile));

		final JMenu importSubmenu = new JMenu(Label.FILE_MENU_IMPORT.label());
		importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_RS_GUITAR, songFileHandler::importRSArrangementXML));
		importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_RS_VOCALS, songFileHandler::importRSVocalsArrangementXML));
		importSubmenu.setEnabled(!data.isEmpty);
		menu.add(importSubmenu);

		menu.addSeparator();
		menu.add(createItem(Label.FILE_MENU_SAVE, ctrl('S'), songFileHandler::save));
		menu.add(createItem(Label.FILE_MENU_SAVE_AS, ctrlShift('S'), songFileHandler::saveAs));

		menu.addSeparator();
		menu.add(createItem(Label.FILE_MENU_EXIT, button(VK_ESCAPE), frame::exit));

		menu.addSeparator();
		menu.add(createItem(Label.FILE_MENU_OPTIONS, () -> new ConfigPane(frame)));

		return menu;
	}

}
