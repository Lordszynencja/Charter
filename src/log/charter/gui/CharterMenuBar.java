package log.charter.gui;

import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_F5;
import static java.awt.event.KeyEvent.VK_PERIOD;
import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.panes.ConfigPane;
import log.charter.gui.panes.SongOptionsPane;
import log.charter.main.LogCharterRSMain;

public class CharterMenuBar extends JMenuBar {

	private static final long serialVersionUID = -5784270027920161709L;

	private static JMenuItem createItem(final String name, final ActionListener listener) {
		final JMenuItem item = new JMenuItem(name);
		item.addActionListener(listener);
		return item;
	}

	private static JMenuItem createItem(final String name, final KeyStroke keyStroke, final ActionListener listener) {
		final JMenuItem item = new JMenuItem(name);
		item.setAccelerator(keyStroke);
		item.addActionListener(listener);
		return item;
	}

	private static KeyStroke button(final int keyCode) {
		return getKeyStroke(keyCode, 0);
	}

	@SuppressWarnings("unused")
	private static KeyStroke altCtrl(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
	}

	private static KeyStroke ctrl(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK);
	}

	private static KeyStroke ctrlShift(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
	}

	private AudioHandler audioHandler;
	private CharterFrame frame;
	private ChartData data;
	private ChartKeyboardHandler chartKeyboardHandler;
	private SongFileHandler songFileHandler;

	private JMenu editMenu;
	private JMenuItem songOptionsItem;
	private JMenu instrumentMenu;
	private JMenu guitarMenu;
	private JMenu vocalsMenu;
	private JMenu notesMenu;

	public void init(final AudioHandler audioHandler, final ChartKeyboardHandler chartKeyboardHandler,
			final CharterFrame frame, final ChartData data, final SongFileHandler songFileHandler) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.chartKeyboardHandler = chartKeyboardHandler;
		this.songFileHandler = songFileHandler;

		final Dimension size = new Dimension(100, 20);
		setMinimumSize(size);
		this.setSize(size);
		setMaximumSize(size);

		this.add(prepareFileMenu());
		this.add(prepareEditMenu());
		this.add(prepareConfigMenu());
		this.add(prepareInstrumentMenu());
		this.add(prepareGuitarMenu());
		this.add(prepareVocalsMenu());
		this.add(prepareNotesMenu());
		this.add(prepareInfoMenu());

		frame.setJMenuBar(this);
	}

	private JMenu prepareFileMenu() {
		final JMenu importSubmenu = new JMenu("Import");
		importSubmenu.add(createItem("Open song from RS arrangement XML",
				e -> songFileHandler.openSongWithImportFromArrangementXML()));
		importSubmenu.add(createItem("RS arrangement XML", e -> songFileHandler.importRSArrangementXML()));
		importSubmenu.add(createItem("RS vocals arrangement XML", e -> songFileHandler.importRSVocalsArrangementXML()));

		final JMenu menu = new JMenu("File");
		menu.add(createItem("New", ctrl('N'), e -> songFileHandler.newSong()));
		menu.add(createItem("Open", ctrl('O'), e -> songFileHandler.open()));
		menu.add(createItem("Open audio file", e -> songFileHandler.openAudioFile()));
		menu.add(importSubmenu);
		menu.add(createItem("Save", ctrl('S'), e -> songFileHandler.save()));
		menu.add(createItem("Save as...", ctrlShift('S'), e -> songFileHandler.saveAs()));
		menu.add(createItem("Exit", button(VK_ESCAPE), e -> chartKeyboardHandler.exit()));

		return menu;
	}

	private JMenu prepareEditMenu() {
		final JMenu menu = new JMenu("Edit");

		menu.add(createItem("Select all", ctrl('A'), e -> chartKeyboardHandler.selectAll()));
		menu.add(createItem("Delete", button(VK_DELETE), e -> chartKeyboardHandler.delete()));
		menu.add(createItem("Undo", ctrl('Z'), e -> chartKeyboardHandler.undo()));
		menu.add(createItem("Redo", ctrl('R'), e -> chartKeyboardHandler.redo()));
		menu.add(createItem("Copy", ctrl('C'), e -> chartKeyboardHandler.copy()));
		menu.add(createItem("Paste", ctrl('V'), e -> chartKeyboardHandler.paste()));
		menu.addSeparator();

		menu.add(createItem("Toggle debug info", e -> chartKeyboardHandler.toggleDrawDebug()));

		menu.setEnabled(false);
		editMenu = menu;
		return menu;
	}

	private JMenu prepareConfigMenu() {
		final JMenu menu = new JMenu("Config");
		menu.add(createItem("Options", e -> new ConfigPane(frame)));

		songOptionsItem = createItem("Song options", e -> new SongOptionsPane(frame, songFileHandler, data));
		songOptionsItem.setEnabled(false);
		menu.add(songOptionsItem);

		return menu;
	}

	public void changeEditMode(final EditMode editMode) {
		audioHandler.stopMusic();

		editMenu.setEnabled(true);
		songOptionsItem.setEnabled(true);
		instrumentMenu.setEnabled(true);
		notesMenu.setEnabled(true);

		guitarMenu.setEnabled(false);
		vocalsMenu.setEnabled(false);

		if (editMode == EditMode.GUITAR) {
			guitarMenu.setEnabled(true);
		} else if (editMode == EditMode.VOCALS) {
			vocalsMenu.setEnabled(true);
		}

		data.changeEditMode(editMode);
	}

	private JMenu prepareInstrumentMenu() {
		final JMenu menu = new JMenu("Instrument");

		for (final EditMode editMode : EditMode.values()) {
			menu.add(createItem(editMode.name(), e -> changeEditMode(editMode)));
		}
		menu.addSeparator();

		menu.add(createItem("Draw waveform", button(VK_F5), e -> chartKeyboardHandler.toggleDrawWaveform()));
		menu.add(createItem("Toggle claps on note", button('C'), e -> audioHandler.toggleClaps()));
		menu.add(createItem("Toggle metronome on measures", button('M'), e -> audioHandler.toggleMetronome()));

		menu.setEnabled(false);

		instrumentMenu = menu;
		return menu;
	}

	private JMenu prepareGuitarMenu() {
		final JMenu menu = new JMenu("Guitar");

		menu.add(createItem("Toggle HO/PO", button('H'), e -> chartKeyboardHandler.toggleHammerOn()));
		menu.add(createItem("Toggle crazy notes", button('U'), e -> chartKeyboardHandler.toggleCrazy()));

		menu.setEnabled(false);

		guitarMenu = menu;
		return menu;
	}

	private JMenu prepareVocalsMenu() {
		final JMenu menu = new JMenu("Vocals");

		menu.add(createItem("Edit lyric", button('L'), e -> chartKeyboardHandler.editLyric()));
		menu.add(createItem("Toggle notes word part", button('W'), e -> chartKeyboardHandler.toggleVocalsWordPart()));
		menu.add(createItem("Toggle notes phrase end", button('E'), e -> chartKeyboardHandler.toggleVocalsPhraseEnd()));

		menu.setEnabled(false);

		vocalsMenu = menu;
		return menu;
	}

	private JMenu prepareNotesMenu() {
		final JMenu menu = new JMenu("Notes");

		menu.add(createItem("Toggle grid", button('G'), e -> chartKeyboardHandler.toggleGrid()));
		menu.add(createItem("Change grid size", e -> chartKeyboardHandler.changeGridSize()));
		menu.add(createItem("Snap notes to grid", ctrl('F'), e -> chartKeyboardHandler.snapNotes()));
		menu.add(createItem("Double grid size", button(VK_PERIOD), e -> chartKeyboardHandler.doubleGridSize()));
		menu.add(createItem("Half grid size", button(VK_COMMA), e -> chartKeyboardHandler.halfGridSize()));

		menu.addSeparator();
		final JMenu copyFromMenu = new JMenu("Copy from");

//		for (final InstrumentType type : InstrumentType.sortedValues()) {
//			final JMenu copyFromMenuInstr = new JMenu(type.name);
//			for (int i = 0; i < Instrument.diffNames.length; i++) {
//				final int diff = i;
//				copyFromMenuInstr.add(createItem(Instrument.diffNames[i], e -> handler.copyFrom(type, diff)));
//			}
//			copyFromMenu.add(copyFromMenuInstr);
//		}

		menu.add(copyFromMenu);

		menu.setEnabled(false);

		notesMenu = menu;
		return menu;
	}

	private JMenu prepareInfoMenu() {
		final JMenu menu = new JMenu("Info");

		final String infoText = "Lords of Games Charter\n"//
				+ "Created by Lordszynencja\n"//
				+ "Current version: " + LogCharterRSMain.VERSION + "\n\n"//
				+ "TODO:\n"//
				+ "working Save As...\n"//
				+ "GP import\n"//
				+ "a lot more";

		menu.add(createItem("Version", e -> JOptionPane.showMessageDialog(frame, infoText)));

		return menu;
	}
}
