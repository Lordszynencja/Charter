package log.charter.gui.menuHandlers;

import static java.awt.event.KeyEvent.VK_ESCAPE;

import javax.swing.JMenu;

import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.panes.ConfigPane;

class FileMenuHandler extends CharterMenuHandler {
	private CharterFrame frame;
	private SongFileHandler songFileHandler;

	public void init(final CharterFrame frame, final SongFileHandler songFileHandler) {
		this.frame = frame;
		this.songFileHandler = songFileHandler;
	}

	@Override
	boolean isApplicable() {
		return true;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu importSubmenu = new JMenu("Import");
		importSubmenu.add(
				createItem("Open song from RS arrangement XML", songFileHandler::openSongWithImportFromArrangementXML));
		importSubmenu.add(createItem("RS arrangement XML", songFileHandler::importRSArrangementXML));
		importSubmenu.add(createItem("RS vocals arrangement XML", songFileHandler::importRSVocalsArrangementXML));

		final JMenu menu = new JMenu("File");
		menu.add(createItem("New", ctrl('N'), songFileHandler::newSong));
		menu.add(createItem("Open", ctrl('O'), songFileHandler::open));
		menu.add(createItem("Open audio file", songFileHandler::openAudioFile));
		menu.add(importSubmenu);

		menu.addSeparator();
		menu.add(createItem("Save", ctrl('S'), songFileHandler::save));
		menu.add(createItem("Save as...", ctrlShift('S'), songFileHandler::saveAs));

		menu.addSeparator();
		menu.add(createItem("Exit", button(VK_ESCAPE), frame::exit));

		menu.addSeparator();
		menu.add(createItem("Options", () -> new ConfigPane(frame)));

		return menu;
	}

}
