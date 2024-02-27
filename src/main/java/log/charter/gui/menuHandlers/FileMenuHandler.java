package log.charter.gui.menuHandlers;

import static log.charter.io.gp.gp5.transformers.GP5BarOrderExtractor.getBarsOrder;
import static log.charter.io.gp.gp5.transformers.GP5FileTempoMapExtractor.getTempoMap;

import java.io.File;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.panes.ColorConfigPane;
import log.charter.gui.panes.ConfigPane;
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
	private ChartData data;
	private CharterFrame frame;
	private Framer framer;
	private CharterMenuBar charterMenuBar;
	private SongFileHandler songFileHandler;

	public void init(final ArrangementFixer arrangementFixer, final ChartData data, final CharterFrame frame,
			final Framer framer, final CharterMenuBar charterMenuBar, final SongFileHandler songFileHandler) {
		this.arrangementFixer = arrangementFixer;
		this.data = data;
		this.frame = frame;
		this.framer = framer;
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
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPEN_RS, songFileHandler::openSongWithImportFromArrangementXML));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPEN_AUDIO, songFileHandler::openAudioFile));

		final JMenu importSubmenu = new JMenu(Label.FILE_MENU_IMPORT.label());
		importSubmenu
				.add(new SpecialMenuItem(Label.FILE_MENU_IMPORT_RS_GUITAR, songFileHandler::importRSArrangementXML));
		importSubmenu.add(
				new SpecialMenuItem(Label.FILE_MENU_IMPORT_RS_VOCALS, songFileHandler::importRSVocalsArrangementXML));
		importSubmenu.add(new SpecialMenuItem(Label.FILE_MENU_IMPORT_GP, this::importGPFile));
		importSubmenu.add(new SpecialMenuItem(Label.FILE_MENU_IMPORT_MIDI_TEMPO, this::importMidiTempo));
		importSubmenu.setEnabled(!data.isEmpty);
		menu.add(importSubmenu);

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_SAVE, "Ctrl-S", songFileHandler::save));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_SAVE_AS, "Ctrl-Shift-S", songFileHandler::saveAs));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_OPTIONS, () -> new ConfigPane(frame, framer)));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_GRAPHIC_OPTIONS, () -> new GraphicConfigPane(frame)));
		menu.add(new SpecialMenuItem(Label.FILE_MENU_COLOR_OPTIONS, () -> new ColorConfigPane(frame)));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.FILE_MENU_EXIT, "Esc", frame::exit));

		return menu;
	}

	private File chooseGP5File() {
		final String dir = data.isEmpty ? Config.songsPath : data.path;
		final File file = FileChooseUtils.chooseFile(frame, dir, new String[] { ".gp3", ".gp4", "gp5" },
				Label.GP_FILE.label());

		return file;
	}

	private boolean askUserAboutUsingExistingTempoMap() {
		final int result = JOptionPane.showConfirmDialog(frame,
				"Do you want to use the tempo map from the imported project?", "GP5 import tempo map",
				JOptionPane.YES_NO_OPTION);
		return JOptionPane.NO_OPTION == result; // when no is selected, use existing tempo map
	}

	private void importGPFile() {
		final File file = chooseGP5File();
		if (file == null) {
			return;
		}

		final boolean useExistingTempoMap = askUserAboutUsingExistingTempoMap();

		try {
			final GP5File gp5File = GP5FileReader.importGPFile(file);
			final List<Integer> barsOrder = getBarsOrder(gp5File.directions, gp5File.masterBars);

			final int startPosition = data.songChart.beatsMap.beats.get(0).position();
			final BeatsMap beatsMap = useExistingTempoMap ? data.songChart.beatsMap
					: getTempoMap(gp5File, startPosition, data.songChart.beatsMap.songLengthMs, barsOrder);

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

		final BeatsMap beatsMap = MidiToBeatsMap.getBeatsMap(file.getAbsolutePath(),
				data.songChart.beatsMap.songLengthMs);
		if (beatsMap == null) {
			Logger.error("Couldn't import tempo from midi file " + file.getAbsolutePath());
			return;
		}

		data.songChart.beatsMap = beatsMap;
	}
}
