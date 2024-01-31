package log.charter.gui.menuHandlers;

import java.io.File;

import javax.swing.JMenu;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.panes.ConfigPane;
import log.charter.gui.panes.GraphicConfigPane;
import log.charter.io.Logger;
import log.charter.io.gp.gp5.GP5File;
import log.charter.io.gp.gp5.GP5FileReader;
import log.charter.util.FileChooseUtils;

public class FileMenuHandler extends CharterMenuHandler {
	private ArrangementFixer arrangementFixer;
	private ChartData data;
	private CharterFrame frame;
	private CharterMenuBar charterMenuBar;
	private SongFileHandler songFileHandler;

	public void init(final ArrangementFixer arrangementFixer, final ChartData data, final CharterFrame frame,
			final CharterMenuBar charterMenuBar, final SongFileHandler songFileHandler) {
		this.arrangementFixer = arrangementFixer;
		this.data = data;
		this.frame = frame;
		this.charterMenuBar = charterMenuBar;
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
		importSubmenu.add(new SpecialMenuItem(Label.FILE_MENU_IMPORT_GP, null, this::importGPFile));
		importSubmenu.setEnabled(!data.isEmpty);
		menu.add(importSubmenu);

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_SAVE, "Ctrl-S", songFileHandler::save));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_SAVE_AS, "Ctrl-Shift-S", songFileHandler::saveAs));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPTIONS, null, () -> new ConfigPane(frame)));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_GRAPHIC_OPTIONS, null, () -> new GraphicConfigPane(frame)));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_EXIT, "Esc", frame::exit));

		return menu;
	}

	private void importGPFile() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File file = FileChooseUtils.chooseFile(frame, dir, new String[] { ".gp3", ".gp4", "gp5" },
				Label.GP_FILE.label());
		if (file == null) {
			return;
		}

		try {
			final GP5File gp5File = GP5FileReader.importGPFile(file);
			data.songChart.addGP5Arrangements(gp5File);
			arrangementFixer.fixArrangement();

			charterMenuBar.refreshMenus();
		} catch (final Exception e) {
			Logger.error("Couldn't import gp5 file " + file.getAbsolutePath(), e);
		}
	}
}
