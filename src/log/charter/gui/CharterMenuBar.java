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

import log.charter.main.LogCharterMain;
import log.charter.song.Instrument;
import log.charter.song.Instrument.InstrumentType;

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

	private static KeyStroke altCtrl(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
	}

	private static KeyStroke ctrl(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK);
	}

	private static KeyStroke ctrlShift(final int keyCode) {
		return getKeyStroke(keyCode, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
	}

	private final ChartEventsHandler handler;

	private JMenu editMenu;
	private JMenuItem songOptionsItem;
	private JMenu instrumentMenu;
	private JMenu guitarMenu;
	private JMenu drumsMenu;
	private JMenu keysMenu;
	private JMenu vocalsMenu;
	private JMenu notesMenu;

	public CharterMenuBar(final ChartEventsHandler handler) {
		super();
		this.handler = handler;
		final Dimension size = new Dimension(100, 20);
		setMinimumSize(size);
		this.setSize(size);
		setMaximumSize(size);

		this.add(prepareFileMenu());
		this.add(prepareEditMenu());
		this.add(prepareConfigMenu());
		this.add(prepareInstrumentMenu());
		this.add(prepareGuitarMenu());
		this.add(prepareDrumsMenu());
		this.add(prepareKeysMenu());
		this.add(prepareVocalsMenu());
		this.add(prepareNotesMenu());
		this.add(prepareInfoMenu());
	}

	private JMenu prepareFileMenu() {
		final JMenu menu = new JMenu("File");
		menu.add(createItem("New", ctrl('N'), e -> handler.songFileHandler.newSong()));
		menu.add(createItem("Open", ctrl('O'), e -> handler.songFileHandler.open()));
		menu.add(createItem("Open audio file", e -> handler.songFileHandler.openAudioFile()));
		menu.add(createItem("Save", ctrl('S'), e -> handler.songFileHandler.save()));
		menu.add(createItem("Save as...", ctrlShift('S'), e -> handler.songFileHandler.saveAs()));
		menu.add(createItem("Exit", button(VK_ESCAPE), e -> handler.exit()));

		return menu;
	}

	private JMenu prepareEditMenu() {
		final JMenu menu = new JMenu("Edit");

		menu.add(createItem("Select all", ctrl('A'), e -> handler.data.selectAll()));
		menu.add(createItem("Delete", button(VK_DELETE), e -> handler.delete()));
		menu.add(createItem("Undo", ctrl('Z'), e -> handler.undo()));
		menu.add(createItem("Redo", ctrl('R'), e -> handler.redo()));
		menu.add(createItem("Copy", ctrl('C'), e -> handler.data.copy()));
		menu.add(createItem("Paste", ctrl('V'), e -> handler.paste()));
		menu.addSeparator();

		menu.add(createItem("Toggle debug info", e -> handler.toggleDrawDebug()));

		menu.setEnabled(false);
		editMenu = menu;
		return menu;
	}

	private JMenu prepareConfigMenu() {
		final JMenu menu = new JMenu("Config");
		menu.add(createItem("Options", e -> new ConfigPane(handler.frame)));

		songOptionsItem = createItem("Song options", e -> new SongOptionsPane(handler.frame));
		songOptionsItem.setEnabled(false);
		menu.add(songOptionsItem);

		return menu;
	}

	public void changeInstrument(final InstrumentType type) {
		editMenu.setEnabled(true);
		songOptionsItem.setEnabled(true);
		instrumentMenu.setEnabled(true);
		notesMenu.setEnabled(true);

		guitarMenu.setEnabled(false);
		drumsMenu.setEnabled(false);
		keysMenu.setEnabled(false);
		vocalsMenu.setEnabled(false);
		if (type.isGuitarType()) {
			guitarMenu.setEnabled(true);
		} else if (type.isDrumsType()) {
			drumsMenu.setEnabled(true);
		} else if (type.isKeysType()) {
			keysMenu.setEnabled(true);
		} else if (type.isVocalsType()) {
			vocalsMenu.setEnabled(true);
		}
	}

	private JMenu prepareInstrumentMenu() {
		final JMenu menu = new JMenu("Instrument");

		for (int i = 0; i < Instrument.diffNames.length; i++) {
			final int diff = i;
			menu.add(createItem(Instrument.diffNames[i], e -> handler.data.changeDifficulty(diff)));
		}
		menu.addSeparator();

		for (final InstrumentType type : InstrumentType.sortedValues()) {
			menu.add(createItem(type.name, e -> handler.data.changeInstrument(type)));
		}
		menu.addSeparator();

		menu.add(createItem("Draw waveform", button(VK_F5), e -> handler.toggleDrawWaveform()));
		menu.add(createItem("Toggle claps on note", button('C'), e -> handler.toggleClaps()));
		menu.add(createItem("Toggle metronome on measures", button('M'), e -> handler.toggleMetronome()));

		menu.setEnabled(false);

		instrumentMenu = menu;
		return menu;
	}

	private JMenu prepareGuitarMenu() {
		final JMenu menu = new JMenu("Guitar");

		menu.add(createItem("Toggle HO/PO", button('H'), e -> handler.toggleHOPO()));
		menu.add(createItem("Toggle HO/PO by distance", ctrl('H'), e -> handler.toggleHOPOByDistance()));
		menu.add(createItem("Toggle crazy notes", button('U'), e -> handler.toggleCrazy()));
		menu.add(createItem("Set star power section", ctrl('W'), e -> handler.setSPSection()));
		menu.add(createItem("Set tap section", ctrl('T'), e -> handler.setTapSection()));
		menu.add(createItem("Set solo section", ctrl('P'), e -> handler.setSoloSection()));

		menu.setEnabled(false);

		guitarMenu = menu;
		return menu;
	}

	private JMenu prepareDrumsMenu() {
		final JMenu menu = new JMenu("Drums");

		menu.add(createItem("Set star power section", ctrl('W'), e -> handler.setSPSection()));
		menu.add(createItem("Set solo section", ctrl('P'), e -> handler.setSoloSection()));
		menu.add(createItem("Set drum roll section", ctrl('K'), e -> handler.setDrumRollSection()));
		menu.add(createItem("Set special drum roll section", ctrl('L'), e -> handler.setSpecialDrumRollSection()));
		menu.addSeparator();

		menu.add(createItem("Toggle expert+ bass", ctrl('E'), e -> handler.toggleExpertPlus()));
		menu.add(createItem("Toggle yellow tom", ctrl('Y'), e -> handler.toggleYellowTom()));
		menu.add(createItem("Toggle blue tom", ctrl('B'), e -> handler.toggleBlueTom()));
		menu.add(createItem("Toggle green tom", ctrl('G'), e -> handler.toggleGreenTom()));
		menu.add(createItem("Toggle yellow tom+cymbal", altCtrl('Y'), e -> handler.toggleYellowTomCymbal()));
		menu.add(createItem("Toggle blue tom+cymbal", altCtrl('B'), e -> handler.toggleBlueTomCymbal()));
		menu.add(createItem("Toggle green tom+cymbal", altCtrl('G'), e -> handler.toggleGreenTomCymbal()));
		menu.addSeparator();

		menu.add(createItem("Generate kick drum", e -> handler.generateKickDrumFromMusic()));
		menu.add(createItem("Generate snare drum", e -> handler.generateSnareDrumFromMusic()));

		menu.setEnabled(false);

		drumsMenu = menu;
		return menu;
	}

	private JMenu prepareKeysMenu() {
		final JMenu menu = new JMenu("Keys");

		menu.add(createItem("Toggle crazy notes", button('U'), e -> handler.toggleCrazy()));
		menu.add(createItem("Set star power section", ctrl('W'), e -> handler.setSPSection()));
		menu.add(createItem("Set solo section", ctrl('P'), e -> handler.setSoloSection()));

		menu.setEnabled(false);

		keysMenu = menu;
		return menu;
	}

	private JMenu prepareVocalsMenu() {
		final JMenu menu = new JMenu("Vocals");

		menu.add(createItem("Edit lyric", button('L'), e -> handler.editLyric()));
		menu.add(createItem("Set lyric line", ctrl('L'), e -> handler.setLyricLine()));
		menu.add(createItem("Toggle notes connected", button('Q'), e -> handler.toggleLyricConnected()));
		menu.add(createItem("Toggle notes toneless", button('T'), e -> handler.toggleLyricToneless()));
		menu.add(createItem("Toggle notes word part", button('W'), e -> handler.toggleLyricWordPart()));

		menu.setEnabled(false);

		vocalsMenu = menu;
		return menu;
	}

	private JMenu prepareNotesMenu() {
		final JMenu menu = new JMenu("Notes");

		menu.add(createItem("Toggle grid", button('G'), e -> handler.toggleGrid()));
		menu.add(createItem("Change grid size", e -> handler.changeGridSize()));
		menu.add(createItem("Snap notes to grid", ctrl('F'), e -> handler.snapNotes()));
		menu.add(createItem("Double grid size", button(VK_PERIOD), e -> handler.doubleGridSize()));
		menu.add(createItem("Half grid size", button(VK_COMMA), e -> handler.halfGridSize()));

		menu.addSeparator();
		final JMenu copyFromMenu = new JMenu("Copy from");

		for (final InstrumentType type : InstrumentType.sortedValues()) {
			final JMenu copyFromMenuInstr = new JMenu(type.name);
			for (int i = 0; i < Instrument.diffNames.length; i++) {
				final int diff = i;
				copyFromMenuInstr.add(createItem(Instrument.diffNames[i], e -> handler.copyFrom(type, diff)));
			}
			copyFromMenu.add(copyFromMenuInstr);
		}

		menu.add(copyFromMenu);

		menu.setEnabled(false);

		notesMenu = menu;
		return menu;
	}

	private JMenu prepareInfoMenu() {
		final JMenu menu = new JMenu("Info");

		final String infoText = "Lords of Games Charter\n"//
				+ "Created by Lordszynencja\n"//
				+ "Current version: " + LogCharterMain.VERSION + "\n\n"//
				+ "TODO:\n"//
				+ "working Save As...\n"//
				+ "own file type/saving song creation progress\n"//
				+ "more features of note editing/selection";

		menu.add(createItem("Version", e -> JOptionPane.showMessageDialog(handler.frame, infoText)));

		return menu;
	}
}
