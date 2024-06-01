package log.charter.gui.menuHandlers;

import java.io.File;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.ShortcutConfigPane;
import log.charter.gui.panes.colorConfig.ColorConfigPane;
import log.charter.gui.panes.graphicalConfig.GraphicConfigPane;
import log.charter.gui.panes.programConfig.ProgramConfigPane;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.data.files.GP5FileImporter;
import log.charter.services.data.files.MidiImporter;
import log.charter.services.data.files.RSXMLImporter;
import log.charter.services.data.files.SongFileHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.utils.Framer;
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
		menu.add(createItem(Label.CREATE_PROJECT_FROM_RS_XML, songFileHandler::createSongWithImportFromArrangementXML));

		if (modeManager.getMode() != EditMode.EMPTY) {
			menu.add(createItem(Label.CHANGE_AUDIO, this::openAudioFile));

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
		menu.add(
				createItem(Label.FILE_MENU_OPTIONS, () -> new ProgramConfigPane(charterFrame, charterContext, framer)));
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
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (file == null) {
			return;
		}

		rsXMLImporter.importAndAddRSArrangementXML(file);
	}

	public void importRSVocalsXML() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (file == null) {
			return;
		}

		rsXMLImporter.importRSVocalsXML(file);
	}

	private void importGPFile() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path,
				new String[] { ".gp3", ".gp4", "gp5" }, Label.GP_FILE.label());
		if (file == null) {
			return;
		}

		gp5FileImporter.importGP5File(file);
	}

	private void importMidiTempo() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".mid" },
				Label.MIDI_FILE.label());
		if (file == null) {
			return;
		}

		midiImporter.importMidiTempo(file);
	}

}
