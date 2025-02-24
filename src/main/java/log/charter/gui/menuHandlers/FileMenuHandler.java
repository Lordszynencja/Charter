package log.charter.gui.menuHandlers;

import java.io.File;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.LoadingDialog;
import log.charter.gui.components.utils.ComponentUtils;
import log.charter.gui.panes.colorConfig.ColorConfigPane;
import log.charter.gui.panes.graphicalConfig.GraphicConfigPane;
import log.charter.gui.panes.programConfig.ConfigPane;
import log.charter.gui.panes.shortcuts.ShortcutConfigPane;
import log.charter.io.gp.gp7.GP7PlusFileImporter;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.data.files.GP5FileImporter;
import log.charter.services.data.files.LRCImporter;
import log.charter.services.data.files.MidiImporter;
import log.charter.services.data.files.RSXMLImporter;
import log.charter.services.data.files.SongFileHandler;
import log.charter.services.data.files.USCTxtImporter;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.utils.Framer;
import log.charter.sound.data.AudioData;
import log.charter.util.FileChooseUtils;

public class FileMenuHandler extends CharterMenuHandler implements Initiable {
	private ActionHandler actionHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterContext charterContext;
	private CharterMenuBar charterMenuBar;
	private Framer framer;
	private GP5FileImporter gp5FileImporter;
	private GP7PlusFileImporter gp7PlusFileImporter;
	private LRCImporter lrcImporter;
	private MidiImporter midiImporter;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private RSXMLImporter rsXMLImporter;
	private SongFileHandler songFileHandler;
	private USCTxtImporter uscTxtImporter;

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
//TODO create projects based on gp5/gp8/GPA files

		if (modeManager.getMode() != EditMode.EMPTY) {
			menu.add(createItem(Label.CHANGE_AUDIO, this::openAudioFile));
			menu.add(createItem(Label.ADD_AUDIO_STEM, this::addAudioStem));

			final JMenu importSubmenu = createMenu(Label.FILE_MENU_IMPORT);
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_MIDI_TEMPO, this::importMidiTempo));
			importSubmenu.addSeparator();

			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_RS_VOCALS, this::importRSVocalsXML));
			importSubmenu.add(createItem(Label.IMPORT_LRC_VOCALS, this::importLRCVocals));
			importSubmenu.add(createItem(Label.IMPORT_USC_VOCALS, this::importUSCVocals));
			importSubmenu.addSeparator();

			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_RS_GUITAR, this::importRSArrangementXML));
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_GP, this::importGPFile));

			menu.add(importSubmenu);

			menu.addSeparator();
			menu.add(createItem(Action.SAVE));
			menu.add(createItem(Action.SAVE_AS));
		}

		menu.addSeparator();
		menu.add(createItem(Label.CONFIG, () -> new ConfigPane(charterFrame, charterContext, framer)));
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

	private void addAudioStem() {
		final File file = FileChooseUtils.chooseMusicFile(charterFrame, chartData.path);
		if (file == null) {
			return;
		}

		String path = file.getAbsolutePath();
		boolean local = false;
		if (path.startsWith(chartData.path)) {
			local = true;
			path = path.substring(chartData.path.length());
		}

		final AudioData stemAudioData = LoadingDialog.load(charterFrame, 1, dialog -> {
			dialog.setProgress(0, Label.LOADING_MUSIC_FILE);
			final AudioData result = AudioData.readFile(file);
			dialog.addProgress(Label.LOADING_DONE);

			return result;
		}, "Loading stem audio");
		if (stemAudioData == null) {
			ComponentUtils.showPopup(charterFrame, Label.COULDNT_LOAD_AUDIO, file.getAbsolutePath());
			return;
		}

		String stemName = ComponentUtils.askForInput(charterFrame, Label.AUDIO_STEM_NAME, "");
		while (stemName != null && stemName.isBlank()) {
			ComponentUtils.showPopup(charterFrame, Label.AUDIO_STEM_NAME_CANT_BE_EMPTY);
			stemName = ComponentUtils.askForInput(charterFrame, Label.AUDIO_STEM_NAME, "");
		}
		if (stemName == null) {
			return;
		}

		projectAudioHandler.addStem(stemName, path, local, stemAudioData);
	}

	private void importMidiTempo() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".mid" },
				Label.MIDI_FILE.label());
		if (file == null) {
			return;
		}

		midiImporter.importMidiTempo(file);
	}

	private void importRSVocalsXML() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (file == null) {
			return;
		}

		rsXMLImporter.importRSVocalsXML(file);
	}

	private void importLRCVocals() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".lrc" },
				Label.LRC_FILE.label());
		if (file == null) {
			return;
		}

		lrcImporter.importLRCFile(file);
	}

	private void importUSCVocals() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".txt" },
				Label.TXT_FILE.label());
		if (file == null) {
			return;
		}

		uscTxtImporter.importUSCFile(file);
	}

	private void importRSArrangementXML() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path, new String[] { ".xml" },
				new String[] { Label.RS_ARRANGEMENT_FILE.label() });
		if (file == null) {
			return;
		}

		rsXMLImporter.importAndAddRSArrangementXML(file);
	}

	private void importGPFile() {
		final File file = FileChooseUtils.chooseFile(charterFrame, chartData.path,
				new String[] { ".gp3", ".gp4", ".gp5", ".gp" }, Label.GP_FILE.label());
		if (file == null) {
			return;
		}

		if (file.getName().endsWith(".gp3") || file.getName().endsWith(".gp4") || file.getName().endsWith(".gp5")) {
			gp5FileImporter.importGP5File(file);
		} else if (file.getName().endsWith(".gp")) {
			gp7PlusFileImporter.importGP7PlusFile(file);
		}
	}
}
