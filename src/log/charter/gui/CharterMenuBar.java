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
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import log.charter.data.ChartData;
import log.charter.data.EditMode;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.KeyboardHandler;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.panes.ConfigPane;
import log.charter.gui.panes.GridPane;
import log.charter.gui.panes.SongOptionsPane;
import log.charter.main.LogCharterRSMain;
import log.charter.song.ArrangementChart;
import log.charter.util.CollectionUtils.HashMap2;

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

	private static KeyStroke ctrl(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK);
	}

	private static KeyStroke ctrlShift(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
	}

	private AudioDrawer audioDrawer;
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private SongFileHandler songFileHandler;
	private UndoSystem undoSystem;

	private final JMenu editMenu;
	private final JMenu fileMenu;
	private final JMenu guitarMenu;
	private final JMenu infoMenu;
	private final JMenu notesMenu;
	private final JMenu vocalsMenu;

	public CharterMenuBar() {
		fileMenu = prepareFileMenu();
		editMenu = prepareEditMenu();
		guitarMenu = prepareGuitarMenu();
		infoMenu = prepareInfoMenu();
		notesMenu = prepareNotesMenu();
		vocalsMenu = prepareVocalsMenu();
	}

	public void init(final AudioDrawer audioDrawer, final AudioHandler audioHandler, final ChartData data,
			final CharterFrame frame, final KeyboardHandler keyboardHandler, final ModeManager modeManager,
			final SelectionManager selectionManager, final SongFileHandler songFileHandler,
			final UndoSystem undoSystem) {
		this.audioDrawer = audioDrawer;
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.songFileHandler = songFileHandler;
		this.undoSystem = undoSystem;

		final Dimension size = new Dimension(100, 20);
		setMinimumSize(size);
		this.setSize(size);
		setMaximumSize(size);

		this.add(fileMenu);
		this.add(infoMenu);

		frame.setJMenuBar(this);
	}

	private JMenu prepareEditMenu() {
		final JMenu menu = new JMenu("Edit");

		menu.add(createItem("Select all", ctrl('A'), e -> keyboardHandler.selectAll()));
		menu.add(createItem("Delete", button(VK_DELETE), e -> keyboardHandler.delete()));
		menu.add(createItem("Undo", ctrl('Z'), e -> undoSystem.undo()));
		menu.add(createItem("Redo", ctrl('R'), e -> undoSystem.redo()));
		menu.add(createItem("Copy", ctrl('C'), e -> keyboardHandler.copy()));
		menu.add(createItem("Paste", ctrl('V'), e -> keyboardHandler.paste()));

		menu.addSeparator();
		menu.add(createItem("Song options", e -> new SongOptionsPane(frame, songFileHandler, data)));
		menu.add(createItem("Grid options", button('G'), e -> new GridPane(frame, data.songChart.beatsMap)));

		return menu;
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

		menu.addSeparator();
		menu.add(createItem("Save", ctrl('S'), e -> songFileHandler.save()));
		menu.add(createItem("Save as...", ctrlShift('S'), e -> songFileHandler.saveAs()));

		menu.addSeparator();
		menu.add(createItem("Exit", button(VK_ESCAPE), e -> frame.exit()));

		menu.addSeparator();
		menu.add(createItem("Options", e -> new ConfigPane(frame)));

		return menu;
	}

	private JMenu prepareInfoMenu() {
		final JMenu menu = new JMenu("Info");

		final String infoText = "Lords of Games Rocksmith Charter\n"//
				+ "Created by Lordszynencja\n"//
				+ "Current version: " + LogCharterRSMain.VERSION + "\n\n"//
				+ "TODO:\n"//
				+ "working Save As...\n"//
				+ "GP import\n"//
				+ "note edit\n"//
				+ "a lot more";

		menu.add(createItem("Version", e -> JOptionPane.showMessageDialog(frame, infoText)));

		return menu;
	}

	private JMenu prepareInstrumentMenu() {
		final JMenu menu = new JMenu("Instrument");

		menu.add(createItem(EditMode.VOCALS.label, e -> changeEditMode(EditMode.VOCALS)));

		final Map<String, Integer> arrangementNumbers = new HashMap2<>();
		for (int i = 0; i < data.songChart.arrangements.size(); i++) {
			final ArrangementChart arrangement = data.songChart.arrangements.get(i);
			String arrangementName = arrangement.getTypeNameLabel();
			final int arrangementNumber = arrangementNumbers.getOrDefault(arrangementName, 1);
			arrangementNumbers.put(arrangementName, arrangementNumber + 1);
			if (arrangementNumber > 1) {
				arrangementName += " " + arrangementNumber;
			}

			final int arrangementId = i;
			menu.add(createItem(arrangementName, e -> {
				data.currentArrangement = arrangementId;
				changeEditMode(EditMode.GUITAR);
			}));
		}
		menu.addSeparator();

		menu.add(createItem("Draw waveform", button(VK_F5), e -> audioDrawer.drawAudio = !audioDrawer.drawAudio));
		menu.add(createItem("Toggle claps on note", button('C'), e -> audioHandler.toggleClaps()));
		menu.add(createItem("Toggle metronome on measures", button('M'), e -> audioHandler.toggleMetronome()));

		return menu;
	}

	private JMenu prepareGuitarMenu() {
		final JMenu menu = new JMenu("Guitar");

		menu.add(createItem("Toggle HO/PO", button('H'), e -> keyboardHandler.toggleHammerOn()));
		menu.add(createItem("Toggle crazy notes", button('U'), e -> keyboardHandler.toggleCrazy()));

		return menu;
	}

	private JMenu prepareNotesMenu() {
		final JMenu menu = new JMenu("Notes");

		menu.add(createItem("Snap notes to grid", ctrl('F'), e -> keyboardHandler.snapNotes()));
		menu.add(createItem("Double grid size", button(VK_PERIOD), e -> data.songChart.beatsMap.gridSize *= 2));
		menu.add(createItem("Half grid size", button(VK_COMMA), e -> {
			if (data.songChart.beatsMap.gridSize % 2 == 0) {
				data.songChart.beatsMap.gridSize /= 2;
			}
		}));

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

		return menu;
	}

	private JMenu prepareVocalsMenu() {
		final JMenu menu = new JMenu("Vocals");

		menu.add(createItem("Edit lyric", button('L'), e -> {
		}));// keyboardHandler.editLyric()));
		menu.add(createItem("Toggle notes word part", button('W'), e -> keyboardHandler.toggleVocalsWordPart()));
		menu.add(createItem("Toggle notes phrase end", button('E'), e -> keyboardHandler.toggleVocalsPhraseEnd()));

		return menu;
	}

	public void changeEditMode(final EditMode editMode) {
		audioHandler.stopMusic();
		selectionManager.clear();

		removeAll();

		this.add(fileMenu);
		this.add(editMenu);
		this.add(prepareInstrumentMenu());

		if (editMode == EditMode.GUITAR) {
			this.add(guitarMenu);
		} else if (editMode == EditMode.VOCALS) {
			this.add(vocalsMenu);
		}

		this.add(notesMenu);
		this.add(infoMenu);

		data.changeDifficulty(0);
		modeManager.editMode = editMode;

		validate();
	}
}
