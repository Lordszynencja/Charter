package log.charter.gui.handlers;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static log.charter.data.config.Config.frets;
import static log.charter.song.notes.IPosition.findFirstAfter;
import static log.charter.song.notes.IPosition.findLastBefore;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.gui.panes.ChordOptionsPane;
import log.charter.gui.panes.NoteOptionsPane;
import log.charter.song.ChordTemplate;
import log.charter.song.notes.Chord;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.chordRecognition.ChordNameSuggester;

public class KeyboardHandler implements KeyListener {
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private SelectionManager selectionManager;
	private SongFileHandler songFileHandler;
	private UndoSystem undoSystem;

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;
	private boolean left = false;
	private boolean right = false;

	private int lastFretNumber = 0;
	private int fretNumberTimer = 0;

	public void init(final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final ModeManager modeManager, final MouseHandler mouseHandler, final SelectionManager selectionManager,
			final SongFileHandler songFileHandler, final UndoSystem undoSystem) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.modeManager = modeManager;
		this.mouseHandler = mouseHandler;
		this.selectionManager = selectionManager;
		this.songFileHandler = songFileHandler;
		this.undoSystem = undoSystem;
	}

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		left = false;
		right = false;
	}

	public void clearFretNumber() {
		lastFretNumber = 0;
		fretNumberTimer = 0;
	}

	private void moveFromArrowKeys() {
		if (!left && !right) {
			return;
		}

		final int speed = (int) (Framer.frameLength * (shift ? 20 : 4) / (ctrl ? 4 : 1));
		int nextTime = data.nextTime - (left ? speed : 0) + (right ? speed : 0);
		nextTime = max(0, min(data.music.msLength(), nextTime));
		frame.setNextTime(nextTime);
	}

	private void decreaseNumberTimer() {
		fretNumberTimer--;
	}

	public void frame() {
		moveFromArrowKeys();
		decreaseNumberTimer();
	}

	public boolean alt() {
		return alt;
	}

	public boolean ctrl() {
		return ctrl;
	}

	public boolean shift() {
		return shift;
	}

	private void handleLeft() {
		if (!alt) {
			left = true;
			return;
		}

		final int newTime = ctrl ? data.songChart.beatsMap.getPositionWithRemovedGrid(data.time, 1)//
				: findLastBefore(data.songChart.beatsMap.beats, data.time).position();
		frame.setNextTime(newTime);
	}

	private void handleRight() {
		if (!alt) {
			right = true;
			return;
		}

		final int newTime = ctrl ? data.songChart.beatsMap.getPositionWithAddedGrid(data.time, 1)//
				: findFirstAfter(data.songChart.beatsMap.beats, data.time).position();
		frame.setNextTime(newTime);
	}

	public void setFret(final int fret) {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final ArrayList2<Selection<ChordOrNote>> selected = selectionAccessor.getSortedSelected();
		for (final Selection<ChordOrNote> selection : selected) {
			if (selection.selectable.isChord()) {
				final Chord chord = selection.selectable.chord;
				final ChordTemplate newTemplate = new ChordTemplate(
						data.getCurrentArrangement().chordTemplates.get(chord.chordId));
				int fretChange = 0;
				for (int i = 0; i < data.currentStrings(); i++) {
					if (newTemplate.frets.get(i) != null) {
						fretChange = fret - newTemplate.frets.get(i);
						break;
					}
				}
				if (fretChange == 0) {
					continue;
				}

				for (final int string : newTemplate.frets.keySet()) {
					final int oldFret = newTemplate.frets.get(string);
					final int newFret = max(0, min(Config.frets, oldFret + fretChange));

					newTemplate.frets.put(string, newFret);
					if (newFret == 0) {
						newTemplate.fingers.remove(string);
					}
				}

				newTemplate.chordName = ChordNameSuggester
						.suggestChordNames(data.getCurrentArrangement().tuning, newTemplate.frets).get(0);
				chord.chordId = data.getCurrentArrangement().getChordTemplateIdWithSave(newTemplate);
			} else {
				selection.selectable.note.fret = fret;
			}
		}
	}

	private void handleNumber(final int number) {
		if (nanoTime() / 1_000_000 <= fretNumberTimer && lastFretNumber * 10 + number <= frets) {
			fretNumberTimer = (int) (nanoTime() / 1_000_000 + 2000);
			lastFretNumber = lastFretNumber * 10 + number;
			setFret(lastFretNumber);
			return;
		}

		fretNumberTimer = (int) (nanoTime() / 1_000_000 + 2000);
		lastFretNumber = number;
		setFret(number);
	}

	private void handleFKey(final KeyEvent e, final int number) {
		boolean capsLock;
		try {
			capsLock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
		} catch (final Exception exception) {
			capsLock = false;
		}
		final boolean shiftOctave = shift ^ capsLock;

		setFret(shiftOctave ? number + 12 : number);
		e.consume();
	}

	private ArrayList2<ChordOrNote> getSelectedNotes() {
		final SelectionAccessor<ChordOrNote> selectedAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);

		return selectedAccessor.getSortedSelected().map(selection -> selection.selectable);
	}

	private void openChordOptionsPopup(final ArrayList2<ChordOrNote> selected) {
		new ChordOptionsPane(data, frame, undoSystem, selected);
	}

	private void openSingleNoteOptionsPopup(final ArrayList2<ChordOrNote> selected) {
		new NoteOptionsPane(data, frame, undoSystem, selected);
	}

	private void noteOptions() {
		if (data.isEmpty || modeManager.editMode != EditMode.GUITAR) {
			return;
		}

		final ArrayList2<ChordOrNote> selected = getSelectedNotes();
		if (selected.isEmpty()) {
			return;
		}

		if (selected.get(0).isChord()) {
			openChordOptionsPopup(selected);
		} else {
			openSingleNoteOptionsPopup(selected);
		}
	}

	private void handleN(final KeyEvent e) {
		if (alt || shift) {
			return;
		}

		if (ctrl) {
			songFileHandler.newSong();
		} else {
			noteOptions();
		}
	}

	private void handleO(final KeyEvent e) {
		if (ctrl && !alt && !shift) {
			songFileHandler.open();
		}
	}

	private void handleS(final KeyEvent e) {
		if (alt || !ctrl) {
			return;
		}

		if (shift) {
			songFileHandler.saveAs();
		} else {
			songFileHandler.save();
		}
	}

	private final Map<Integer, Consumer<KeyEvent>> keyPressBehaviors = new HashMap<>();

	{
		keyPressBehaviors.put(KeyEvent.VK_SPACE, e -> audioHandler.switchMusicPlayStatus());
		keyPressBehaviors.put(KeyEvent.VK_CONTROL, e -> ctrl = true);
		keyPressBehaviors.put(KeyEvent.VK_ALT, e -> {
			alt = true;
			e.consume();
		});
		keyPressBehaviors.put(KeyEvent.VK_SHIFT, e -> shift = true);
		keyPressBehaviors.put(KeyEvent.VK_HOME, e -> modeManager.getHandler().handleHome());
		keyPressBehaviors.put(KeyEvent.VK_END, e -> modeManager.getHandler().handleEnd());
		keyPressBehaviors.put(KeyEvent.VK_LEFT, e -> handleLeft());
		keyPressBehaviors.put(KeyEvent.VK_RIGHT, e -> handleRight());
		keyPressBehaviors.put(KeyEvent.VK_N, this::handleN);
		keyPressBehaviors.put(KeyEvent.VK_O, this::handleO);
		keyPressBehaviors.put(KeyEvent.VK_S, this::handleS);
		keyPressBehaviors.put(KeyEvent.VK_ESCAPE, e -> frame.exit());
		keyPressBehaviors.put(KeyEvent.VK_1, e -> handleNumber(1));
		keyPressBehaviors.put(KeyEvent.VK_2, e -> handleNumber(2));
		keyPressBehaviors.put(KeyEvent.VK_3, e -> handleNumber(3));
		keyPressBehaviors.put(KeyEvent.VK_4, e -> handleNumber(4));
		keyPressBehaviors.put(KeyEvent.VK_5, e -> handleNumber(5));
		keyPressBehaviors.put(KeyEvent.VK_6, e -> handleNumber(6));
		keyPressBehaviors.put(KeyEvent.VK_7, e -> handleNumber(7));
		keyPressBehaviors.put(KeyEvent.VK_8, e -> handleNumber(8));
		keyPressBehaviors.put(KeyEvent.VK_9, e -> handleNumber(9));
		keyPressBehaviors.put(KeyEvent.VK_0, e -> handleNumber(0));
		keyPressBehaviors.put(KeyEvent.VK_F1, e -> handleFKey(e, 1));
		keyPressBehaviors.put(KeyEvent.VK_F2, e -> handleFKey(e, 2));
		keyPressBehaviors.put(KeyEvent.VK_F3, e -> handleFKey(e, 3));
		keyPressBehaviors.put(KeyEvent.VK_F4, e -> handleFKey(e, 4));
		keyPressBehaviors.put(KeyEvent.VK_F5, e -> handleFKey(e, 5));
		keyPressBehaviors.put(KeyEvent.VK_F6, e -> handleFKey(e, 6));
		keyPressBehaviors.put(KeyEvent.VK_F7, e -> handleFKey(e, 7));
		keyPressBehaviors.put(KeyEvent.VK_F8, e -> handleFKey(e, 8));
		keyPressBehaviors.put(KeyEvent.VK_F9, e -> handleFKey(e, 9));
		keyPressBehaviors.put(KeyEvent.VK_F10, e -> handleFKey(e, 10));
		keyPressBehaviors.put(KeyEvent.VK_F11, e -> handleFKey(e, 11));
		keyPressBehaviors.put(KeyEvent.VK_F12, e -> handleFKey(e, 12));
		keyPressBehaviors.put(KeyEvent.VK_OPEN_BRACKET, e -> audioHandler.toggleClaps());
		keyPressBehaviors.put(KeyEvent.VK_CLOSE_BRACKET, e -> audioHandler.toggleMetronome());
	}

	private static final List<Integer> keysNotClearingMousePressesOnPress = asList(//
			KeyEvent.VK_CONTROL, //
			KeyEvent.VK_ALT, //
			KeyEvent.VK_SHIFT, //
			KeyEvent.VK_CAPS_LOCK, //
			KeyEvent.VK_LEFT, //
			KeyEvent.VK_RIGHT);
	private static final List<Integer> keysNotStoppingMusicOnPress = asList(//
			KeyEvent.VK_CONTROL, //
			KeyEvent.VK_ALT, //
			KeyEvent.VK_SHIFT, //
			KeyEvent.VK_CAPS_LOCK, //
			KeyEvent.VK_F5, //
			KeyEvent.VK_C, //
			KeyEvent.VK_M, //
			KeyEvent.VK_SPACE, //
			KeyEvent.VK_OPEN_BRACKET, //
			KeyEvent.VK_CLOSE_BRACKET);

	private void keyUsed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UNDEFINED) {
			return;
		}

		if (data.isEmpty) {
			switch (keyCode) {
			case KeyEvent.VK_CONTROL:
				ctrl = true;
				break;
			case KeyEvent.VK_ALT:
				alt = true;
				break;
			case KeyEvent.VK_SHIFT:
				shift = true;
				break;
			default:
				break;
			}

			return;
		}

		if (!keysNotClearingMousePressesOnPress.contains(keyCode)) {
			mouseHandler.cancelAllActions();
		}

		if (!keysNotStoppingMusicOnPress.contains(keyCode)) {
			audioHandler.stopMusic();
		}

		keyPressBehaviors.getOrDefault(keyCode, x -> {
		}).accept(e);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F10) {
			return;
		}

		keyUsed(e);
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			left = false;
			break;
		case KeyEvent.VK_RIGHT:
			right = false;
			break;
		case KeyEvent.VK_CONTROL:
			ctrl = false;
			break;
		case KeyEvent.VK_ALT:
			alt = false;
			break;
		case KeyEvent.VK_SHIFT:
			shift = false;
			break;
		case KeyEvent.VK_F10:
			keyUsed(e);
			break;
		default:
			break;
		}
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
