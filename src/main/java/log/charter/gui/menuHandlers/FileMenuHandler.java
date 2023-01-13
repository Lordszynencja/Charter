package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SpecialMenuItem;
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
		menu.add(new SpecialMenuItem(Label.FILE_MENU_NEW, "Ctrl-N", songFileHandler::newSong));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPEN, "Ctrl-O", songFileHandler::open));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPEN_RS, null,
				songFileHandler::openSongWithImportFromArrangementXML));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPEN_AUDIO, null, songFileHandler::openAudioFile));

		final JMenu importSubmenu = new JMenu(Label.FILE_MENU_IMPORT.label());
		importSubmenu.add(
				new SpecialMenuItem(Label.FILE_MENU_IMPORT_RS_GUITAR, null, songFileHandler::importRSArrangementXML));
		importSubmenu.add(new SpecialMenuItem(Label.FILE_MENU_IMPORT_RS_VOCALS, null,
				songFileHandler::importRSVocalsArrangementXML));
		importSubmenu.setEnabled(!data.isEmpty);
		menu.add(importSubmenu);

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_SAVE, "Ctrl-S", songFileHandler::save));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_SAVE_AS, "Ctrl-Shift-S", songFileHandler::saveAs));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_EXIT, "Esc", frame::exit));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPTIONS, null, () -> new ConfigPane(frame)));

		return menu;
	}

}
