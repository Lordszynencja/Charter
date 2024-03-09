package log.charter.gui.menuHandlers;

import java.io.File;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.CharterContext;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.handlers.files.GP5FileImporter;
import log.charter.gui.handlers.files.MidiImporter;
import log.charter.gui.handlers.files.RSXMLImporter;
import log.charter.gui.handlers.files.SongFileHandler;
import log.charter.gui.panes.ConfigPane;
import log.charter.gui.panes.ShortcutConfigPane;
import log.charter.gui.panes.colorConfig.ColorConfigPane;
import log.charter.gui.panes.graphicalConfig.GraphicConfigPane;
import log.charter.gui.utils.Framer;
import log.charter.util.FileChooseUtils;

public class FileMenuHandler extends CharterMenuHandler implements Initiable {
	private ActionHandler actionHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterContext charterContext;
	private CharterMenuBar charterMenuBar;
	private Framer framer;
	private GP5FileImporter gp5FileImporter;
	private MidiImporter midiImporter;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private RSXMLImporter rsXMLImporter;
	private SongFileHandler songFileHandler;

	@Override
	boolean isApplicable() {
		return true;
	}

	@Override
	public void init() {
		super.init(actionHandler);
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.FILE_MENU);
		menu.add(createItem(Action.NEW_PROJECT));
		menu.add(createItem(Action.OPEN_PROJECT));
		menu.add(createItem(Label.MAKE_PROJECT_FROM_RS_XML, songFileHandler::openSongWithImportFromArrangementXML));

		if (modeManager.getMode() != EditMode.EMPTY) {
			menu.add(createItem(Label.FILE_MENU_OPEN_AUDIO, this::openAudioFile));

			final JMenu importSubmenu = createMenu(Label.FILE_MENU_IMPORT);
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_RS_GUITAR, this::importRSArrangementXML));
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_RS_VOCALS, this::importRSVocalsXML));
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_GP, this::importGPFile));
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_MIDI_TEMPO, this::importMidiTempo));
			menu.add(importSubmenu);

			menu.addSeparator();
			menu.add(createItem(Action.SAVE));
			menu.add(createItem(Action.SAVE_AS));
		}

		menu.addSeparator();
		menu.add(createItem(Label.FILE_MENU_OPTIONS, () -> new ConfigPane(charterFrame, framer)));
		menu.add(createItem(Label.SHORTCUT_CONFIG, () -> new ShortcutConfigPane(charterMenuBar, charterFrame)));
		menu.add(
				createItem(Label.FILE_MENU_GRAPHIC_OPTIONS, () -> new GraphicConfigPane(charterFrame, charterContext)));
		menu.add(createItem(Label.FILE_MENU_COLOR_OPTIONS, () -> new ColorConfigPane(charterFrame)));

		menu.addSeparator();
		menu.add(createItem(Action.EXIT));

		return menu;
	}

	private void openAudioFile() {
		final File file = FileChooseUtils.chooseMusicFile(charterFrame, chartData.path);
		if (file == null) {
			return;
		}

		projectAudioHandler.importAudio(file);
	}

	private void importRSArrangementXML() {
		final String dir = chartData.isEmpty ? Config.songsPath : chartData.path;
		final File file = FileChooseUtils.chooseFile(charterFrame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (file == null) {
			return;
		}

		rsXMLImporter.importAndAddRSArrangementXML(file);
	}

	public void importRSVocalsXML() {
		final String dir = chartData.isEmpty ? Config.songsPath : chartData.path;
		final File file = FileChooseUtils.chooseFile(charterFrame, dir, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (file == null) {
			return;
		}

		rsXMLImporter.importRSVocalsXML(file);
	}

	private File chooseGP5File() {
		final String dir = chartData.isEmpty ? Config.songsPath : chartData.path;
		final File file = FileChooseUtils.chooseFile(charterFrame, dir, new String[] { ".gp3", ".gp4", "gp5" },
				Label.GP_FILE.label());

		return file;
	}

	private void importGPFile() {
		final File file = chooseGP5File();
		if (file == null) {
			return;
		}

		gp5FileImporter.importGP5File(file);
	}

	private void importMidiTempo() {
		final String dir = chartData.isEmpty ? Config.songsPath : chartData.path;
		final File file = FileChooseUtils.chooseFile(charterFrame, dir, new String[] { ".mid" },
				Label.MIDI_FILE.label());
		if (file == null) {
			return;
		}

		midiImporter.importMidiTempo(file);
	}

}
