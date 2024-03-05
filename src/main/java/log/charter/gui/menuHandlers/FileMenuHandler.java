package log.charter.gui.menuHandlers;

import static log.charter.gui.components.utils.ComponentUtils.askYesNo;
import static log.charter.io.gp.gp5.transformers.GP5BarOrderExtractor.getBarsOrder;
import static log.charter.io.gp.gp5.transformers.GP5FileTempoMapExtractor.getTempoMap;

import java.io.File;
import java.util.List;

import javax.swing.JMenu;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.gui.handlers.Action;
import log.charter.gui.handlers.ActionHandler;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.panes.ConfigPane;
import log.charter.gui.panes.ShortcutConfigPane;
import log.charter.gui.panes.colorConfig.ColorConfigPane;
import log.charter.gui.panes.graphicalConfig.GraphicConfigPane;
import log.charter.gui.panes.imports.GP5ImportOptions;
import log.charter.io.Logger;
import log.charter.io.gp.gp5.GP5FileReader;
import log.charter.io.gp.gp5.data.GP5File;
import log.charter.io.gp.gp5.transformers.GP5FileToSongChart;
import log.charter.io.midi.MidiToBeatsMap;
import log.charter.song.BeatsMap;
import log.charter.song.SongChart;
import log.charter.util.FileChooseUtils;

public class FileMenuHandler extends CharterMenuHandler {
	private ArrangementFixer arrangementFixer;
	private ChartTimeHandler chartTimeHandler;
	private ChartData data;
	private CharterFrame frame;
	private Framer framer;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SongFileHandler songFileHandler;

	public void init(final ActionHandler actionHandler, final ArrangementFixer arrangementFixer,
			final ChartTimeHandler chartTimeHandler, final ChartData data, final CharterFrame frame,
			final Framer framer, final CharterMenuBar charterMenuBar, final ModeManager modeManager,
			final SongFileHandler songFileHandler) {
		super.init(actionHandler);
		this.arrangementFixer = arrangementFixer;
		this.chartTimeHandler = chartTimeHandler;
		this.data = data;
		this.frame = frame;
		this.framer = framer;
		this.charterMenuBar = charterMenuBar;
		this.modeManager = modeManager;
		this.songFileHandler = songFileHandler;
	}

	@Override
	boolean isApplicable() {
		return true;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.FILE_MENU);
		menu.add(createItem(Action.NEW_PROJECT));
		menu.add(createItem(Action.OPEN_PROJECT));
		menu.add(createItem(Label.MAKE_PROJECT_FROM_RS_XML, songFileHandler::openSongWithImportFromArrangementXML));

		if (modeManager.getMode() != EditMode.EMPTY) {
			menu.add(createItem(Label.FILE_MENU_OPEN_AUDIO, songFileHandler::openAudioFile));

			final JMenu importSubmenu = createMenu(Label.FILE_MENU_IMPORT);
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_RS_GUITAR, songFileHandler::importRSArrangementXML));
			importSubmenu
					.add(createItem(Label.FILE_MENU_IMPORT_RS_VOCALS, songFileHandler::importRSVocalsArrangementXML));
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_GP, this::importGPFile));
			importSubmenu.add(createItem(Label.FILE_MENU_IMPORT_MIDI_TEMPO, this::importMidiTempo));
			menu.add(importSubmenu);

			menu.addSeparator();
			menu.add(createItem(Action.SAVE));
			menu.add(createItem(Action.SAVE_AS));
		}

		menu.addSeparator();
		menu.add(createItem(Label.FILE_MENU_OPTIONS, () -> new ConfigPane(frame, framer)));
		menu.add(createItem(Label.SHORTCUT_CONFIG, () -> new ShortcutConfigPane(charterMenuBar, frame)));
		menu.add(createItem(Label.FILE_MENU_GRAPHIC_OPTIONS, () -> new GraphicConfigPane(frame)));
		menu.add(createItem(Label.FILE_MENU_COLOR_OPTIONS, () -> new ColorConfigPane(frame)));

		menu.addSeparator();
		menu.add(createItem(Action.EXIT));

		return menu;
	}

	private File chooseGP5File() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File file = FileChooseUtils.chooseFile(frame, dir, new String[] { ".gp3", ".gp4", "gp5" },
				Label.GP_FILE.label());

		return file;
	}

	private boolean askUserAboutUsingImportTempoMap() {
		return switch (askYesNo(frame, Label.GP5_IMPORT_TEMPO_MAP, Label.USE_TEMPO_MAP_FROM_IMPORT)) {
			case YES -> true;
			default -> false;
		};
	}

	private void importGPFile() {
		final File file = chooseGP5File();
		if (file == null) {
			return;
		}

		final boolean useImportTempoMap = askUserAboutUsingImportTempoMap();

		try {
			final GP5File gp5File = GP5FileReader.importGPFile(file);
			final List<Integer> barsOrder = getBarsOrder(gp5File.directions, gp5File.masterBars);

			final int startPosition = data.songChart.beatsMap.beats.get(0).position();
			final BeatsMap beatsMap;
			if (useImportTempoMap) {
				beatsMap = getTempoMap(gp5File, startPosition, chartTimeHandler.audioLength(), barsOrder);
			} else {
				beatsMap = data.songChart.beatsMap;
			}

			final SongChart temporaryChart = GP5FileToSongChart.transform(gp5File, beatsMap, barsOrder);

			new GP5ImportOptions(frame, arrangementFixer, charterMenuBar, data, temporaryChart);
		} catch (final Exception e) {
			Logger.error("Couldn't import gp5 file " + file.getAbsolutePath(), e);
		}
	}

	private void importMidiTempo() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File file = FileChooseUtils.chooseFile(frame, dir, new String[] { ".mid" }, Label.MIDI_FILE.label());
		if (file == null) {
			return;
		}

		final BeatsMap beatsMap = MidiToBeatsMap.getBeatsMap(file.getAbsolutePath(), chartTimeHandler.audioLength());
		if (beatsMap == null) {
			Logger.error("Couldn't import tempo from midi file " + file.getAbsolutePath());
			return;
		}

		data.songChart.beatsMap = beatsMap;
	}
}
