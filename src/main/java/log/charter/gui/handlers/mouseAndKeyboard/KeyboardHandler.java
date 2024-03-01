package log.charter.gui.handlers.mouseAndKeyboard;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_1;
import static java.awt.event.KeyEvent.VK_2;
import static java.awt.event.KeyEvent.VK_3;
import static java.awt.event.KeyEvent.VK_4;
import static java.awt.event.KeyEvent.VK_5;
import static java.awt.event.KeyEvent.VK_6;
import static java.awt.event.KeyEvent.VK_7;
import static java.awt.event.KeyEvent.VK_8;
import static java.awt.event.KeyEvent.VK_9;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_NUMPAD0;
import static java.awt.event.KeyEvent.VK_NUMPAD1;
import static java.awt.event.KeyEvent.VK_NUMPAD2;
import static java.awt.event.KeyEvent.VK_NUMPAD3;
import static java.awt.event.KeyEvent.VK_NUMPAD4;
import static java.awt.event.KeyEvent.VK_NUMPAD5;
import static java.awt.event.KeyEvent.VK_NUMPAD6;
import static java.awt.event.KeyEvent.VK_NUMPAD7;
import static java.awt.event.KeyEvent.VK_NUMPAD8;
import static java.awt.event.KeyEvent.VK_NUMPAD9;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static log.charter.data.config.Config.frets;
import static log.charter.song.notes.IConstantPosition.findFirstIdAfter;
import static log.charter.song.notes.IConstantPosition.findLastIdBefore;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ArrangementFixer;
import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.copySystem.CopyManager;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.RepeatManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.Selection;
import log.charter.data.managers.selection.SelectionAccessor;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.types.PositionType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.Framer;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.SongFileHandler;
import log.charter.gui.handlers.data.ChartItemsHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.data.GuitarSoundsHandler;
import log.charter.gui.handlers.data.GuitarSoundsStatusesHandler;
import log.charter.gui.handlers.data.ProjectAudioHandler;
import log.charter.gui.handlers.data.VocalsHandler;
import log.charter.gui.panes.songEdits.HandShapePane;
import log.charter.song.ChordTemplate;
import log.charter.song.HandShape;
import log.charter.song.notes.ChordOrNote;
import log.charter.util.CollectionUtils.ArrayList2;

public class KeyboardHandler implements KeyListener {
	private AudioHandler audioHandler;
	private ChartTimeHandler chartTimeHandler;
	private ChartToolbar chartToolbar;
	private CopyManager copyManager;
	private ChartData data;
	private CharterFrame frame;
	private Framer framer;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private ProjectAudioHandler projectAudioHandler;
	private RepeatManager repeatManager;
	private SelectionManager selectionManager;
	private SongFileHandler songFileHandler;
	private UndoSystem undoSystem;
	private WaveFormDrawer waveFormDrawer;

	private final ChartItemsHandler chartItemsHandler = new ChartItemsHandler();
	private final GuitarSoundsHandler guitarSoundsHandler = new GuitarSoundsHandler();
	private final GuitarSoundsStatusesHandler guitarSoundsStatusesHandler = new GuitarSoundsStatusesHandler();
	private final VocalsHandler vocalsHandler = new VocalsHandler();

	private boolean ctrl = false;
	private boolean alt = false;
	private boolean shift = false;

	private int heldNonModifierKey = -1;
	private Action heldAction = null;

	private int lastFretNumber = 0;
	private int fretNumberTimer = 0;

	public void init(final WaveFormDrawer waveFormDrawer, final AudioHandler audioHandler,
			final ArrangementFixer arrangementFixer, final ChartTimeHandler chartTimeHandler,
			final ChartToolbar chartToolbar, final CopyManager copyManager, final ChartData data,
			final CharterFrame frame, final Framer framer, final ModeManager modeManager,
			final MouseHandler mouseHandler, final ProjectAudioHandler projectAudioHandler,
			final RepeatManager repeatManager, final SelectionManager selectionManager,
			final SongFileHandler songFileHandler, final UndoSystem undoSystem) {
		this.audioHandler = audioHandler;
		this.chartTimeHandler = chartTimeHandler;
		this.chartToolbar = chartToolbar;
		this.copyManager = copyManager;
		this.data = data;
		this.frame = frame;
		this.framer = framer;
		this.modeManager = modeManager;
		this.mouseHandler = mouseHandler;
		this.projectAudioHandler = projectAudioHandler;
		this.repeatManager = repeatManager;
		this.selectionManager = selectionManager;
		this.songFileHandler = songFileHandler;
		this.undoSystem = undoSystem;
		this.waveFormDrawer = waveFormDrawer;

		chartItemsHandler.init(arrangementFixer, data, modeManager, selectionManager, undoSystem);
		guitarSoundsHandler.init(data, frame, selectionManager, undoSystem);
		guitarSoundsStatusesHandler.init(data, frame, selectionManager, undoSystem);
		vocalsHandler.init(data, frame, selectionManager, undoSystem);

		prepareHandlers();
	}

	public void clearKeys() {
		ctrl = false;
		alt = false;
		shift = false;
		heldNonModifierKey = -1;
		heldAction = null;
	}

	public void clearFretNumber() {
		lastFretNumber = 0;
		fretNumberTimer = 0;
	}

	private void moveFromArrowKeys() {
		if (heldAction == null) {
			return;
		}

		double speed;
		switch (heldAction) {
			case FAST_BACKWARD:
				speed = -framer.frameLength * 32;
				break;
			case FAST_FORWARD:
				speed = framer.frameLength * 32;
				break;
			case MOVE_BACKWARD:
				speed = -framer.frameLength * 4;
				break;
			case MOVE_FORWARD:
				speed = framer.frameLength * 4;
				break;
			case SLOW_BACKWARD:
				speed = -framer.frameLength;
				break;
			case SLOW_FORWARD:
				speed = framer.frameLength;
				break;
			default:
				return;
		}

		int nextTime = chartTimeHandler.time() + (int) speed;
		nextTime = max(0, min(projectAudioHandler.getAudio().msLength(), nextTime));
		chartTimeHandler.setNextTime(nextTime);
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

	private void markHandShape() {
		final SelectionAccessor<ChordOrNote> selectionAccessor = selectionManager
				.getSelectedAccessor(PositionType.GUITAR_NOTE);
		if (!selectionAccessor.isSelected()) {
			return;
		}

		undoSystem.addUndo();

		final ArrayList2<HandShape> handShapes = data.getCurrentArrangementLevel().handShapes;
		final ArrayList2<Selection<ChordOrNote>> selected = selectionAccessor.getSortedSelected();
		final int position = selected.get(0).selectable.position();
		final int endPosition = selected.getLast().selectable.endPosition();

		int deleteFromId = findLastIdBefore(handShapes, position);
		if (deleteFromId == -1) {
			deleteFromId = 0;
		}
		if (handShapes.size() > deleteFromId && handShapes.get(deleteFromId).endPosition() < position) {
			deleteFromId++;
		}

		final int firstIdAfter = findFirstIdAfter(handShapes, endPosition);
		final int deleteToId = firstIdAfter == -1 ? handShapes.size() - 1 : firstIdAfter - 1;
		for (int i = deleteToId; i >= deleteFromId; i--) {
			handShapes.remove(i);
		}

		ChordTemplate chordTemplate = new ChordTemplate();
		if (selected.get(0).selectable.isChord()) {
			chordTemplate = data.getCurrentArrangement().chordTemplates
					.get(selected.get(0).selectable.chord.templateId());
		}

		final HandShape handShape = new HandShape(position, endPosition - position);
		handShape.templateId = data.getCurrentArrangement().getChordTemplateIdWithSave(chordTemplate);

		handShapes.add(handShape);
		handShapes.sort(null);
		new HandShapePane(data, frame, handShape, () -> {
			undoSystem.undo();
			undoSystem.removeRedo();
		});
	}

	private void doubleGridSize() {
		if (Config.gridSize <= 512) {
			Config.gridSize *= 2;
			Config.markChanged();

			chartToolbar.updateValues();
		}
	}

	private void halveGridSize() {
		if (Config.gridSize % 2 == 0) {
			Config.gridSize /= 2;
			Config.markChanged();

			chartToolbar.updateValues();
		}
	}

	private void handleFretNumber(final int number) {
		if (nanoTime() / 1_000_000 <= fretNumberTimer && lastFretNumber * 10 + number <= frets) {
			lastFretNumber = lastFretNumber * 10 + number;
		} else {
			lastFretNumber = number;
		}

		fretNumberTimer = (int) (nanoTime() / 1_000_000 + 2000);
		guitarSoundsHandler.setFret(lastFretNumber);
	}

	private void toggleBookmark(final int number) {
		final Integer currentBookmark = data.songChart.bookmarks.get(number);
		if (currentBookmark == null || currentBookmark != chartTimeHandler.time()) {
			data.songChart.bookmarks.put(number, chartTimeHandler.time());
		} else {
			data.songChart.bookmarks.remove(number);
		}
	}

	private void moveToBookmark(final int number) {
		final Integer bookmark = data.songChart.bookmarks.get(number);
		if (bookmark == null) {
			return;
		}

		chartTimeHandler.setNextTime(bookmark);
	}

	private final Map<Action, Runnable> actionHandlers = new HashMap<>();

	private void prepareHandlers() {
		actionHandlers.put(Action.COPY, copyManager::copy);
		actionHandlers.put(Action.DELETE, chartItemsHandler::delete);
		actionHandlers.put(Action.DOUBLE_GRID, this::doubleGridSize);
		actionHandlers.put(Action.EDIT_VOCALS, vocalsHandler::editVocals);
		actionHandlers.put(Action.EXIT, frame::exit);
		actionHandlers.put(Action.HALVE_GRID, this::halveGridSize);
		actionHandlers.put(Action.MARK_HAND_SHAPE, this::markHandShape);
		actionHandlers.put(Action.MOVE_STRING_DOWN, () -> guitarSoundsHandler.moveStringsWithFretChange(-1));
		actionHandlers.put(Action.MOVE_STRING_DOWN_SIMPLE, () -> guitarSoundsHandler.moveStringsWithoutFretChange(-1));
		actionHandlers.put(Action.MOVE_STRING_UP, () -> guitarSoundsHandler.moveStringsWithFretChange(1));
		actionHandlers.put(Action.MOVE_STRING_UP_SIMPLE, () -> guitarSoundsHandler.moveStringsWithoutFretChange(1));
		actionHandlers.put(Action.MOVE_FRET_DOWN, () -> guitarSoundsHandler.moveFret(-1));
		actionHandlers.put(Action.MOVE_FRET_UP, () -> guitarSoundsHandler.moveFret(1));
		actionHandlers.put(Action.MOVE_TO_END, chartTimeHandler::moveToEnd);
		actionHandlers.put(Action.MOVE_TO_FIRST_ITEM, chartTimeHandler::moveToFirstItem);
		actionHandlers.put(Action.MOVE_TO_LAST_ITEM, chartTimeHandler::moveToLastItem);
		actionHandlers.put(Action.MOVE_TO_START, chartTimeHandler::moveToBeginning);
		actionHandlers.put(Action.NEW_PROJECT, songFileHandler::newSong);
		actionHandlers.put(Action.NEXT_BEAT, chartTimeHandler::moveToNextBeat);
		actionHandlers.put(Action.NEXT_GRID, chartTimeHandler::moveToNextGrid);
		actionHandlers.put(Action.NEXT_ITEM, chartTimeHandler::moveToNextItem);
		actionHandlers.put(Action.OPEN_PROJECT, songFileHandler::open);
		actionHandlers.put(Action.PASTE, copyManager::paste);
		actionHandlers.put(Action.PLAY_AUDIO, audioHandler::togglePlaySetSpeed);
		actionHandlers.put(Action.PREVIOUS_BEAT, chartTimeHandler::moveToPreviousBeat);
		actionHandlers.put(Action.PREVIOUS_GRID, chartTimeHandler::moveToPreviousGrid);
		actionHandlers.put(Action.PREVIOUS_ITEM, chartTimeHandler::moveToPreviousItem);
		actionHandlers.put(Action.REDO, undoSystem::redo);
		actionHandlers.put(Action.SAVE, songFileHandler::save);
		actionHandlers.put(Action.SAVE_AS, songFileHandler::saveAs);
		actionHandlers.put(Action.SELECT_ALL_NOTES, selectionManager::selectAllNotes);
		actionHandlers.put(Action.SNAP_ALL, chartItemsHandler::snapAll);
		actionHandlers.put(Action.SNAP_SELECTED, chartItemsHandler::snapSelected);
		actionHandlers.put(Action.SPECIAL_PASTE, copyManager::specialPaste);
		actionHandlers.put(Action.TOGGLE_ACCENT, guitarSoundsStatusesHandler::toggleAccent);
		actionHandlers.put(Action.TOGGLE_ACCENT_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleAccentIndependently);
		actionHandlers.put(Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW, frame::switchBorderlessWindowedPreview);
		actionHandlers.put(Action.TOGGLE_CLAPS, audioHandler::toggleClaps);
		actionHandlers.put(Action.TOGGLE_HARMONIC, guitarSoundsStatusesHandler::toggleHarmonic);
		actionHandlers.put(Action.TOGGLE_HARMONIC_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleHarmonicIndependently);
		actionHandlers.put(Action.TOGGLE_HOPO, guitarSoundsStatusesHandler::toggleHOPO);
		actionHandlers.put(Action.TOGGLE_HOPO_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleHOPOIndependently);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT, guitarSoundsStatusesHandler::toggleLinkNext);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleLinkNextIndependently);
		actionHandlers.put(Action.TOGGLE_METRONOME, audioHandler::toggleMetronome);
		actionHandlers.put(Action.TOGGLE_MIDI, audioHandler::toggleMidiNotes);
		actionHandlers.put(Action.TOGGLE_MUTE, guitarSoundsStatusesHandler::toggleMute);
		actionHandlers.put(Action.TOGGLE_MUTE_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleMuteIndependently);
		actionHandlers.put(Action.TOGGLE_PHRASE_END, vocalsHandler::togglePhraseEnd);
		actionHandlers.put(Action.TOGGLE_PREVIEW_WINDOW, frame::switchWindowedPreview);
		actionHandlers.put(Action.TOGGLE_REPEAT_START, repeatManager::toggleRepeatStart);
		actionHandlers.put(Action.TOGGLE_REPEAT_END, repeatManager::toggleRepeatEnd);
		actionHandlers.put(Action.TOGGLE_REPEATER, repeatManager::toggle);
		actionHandlers.put(Action.TOGGLE_TREMOLO, guitarSoundsStatusesHandler::toggleTremolo);
		actionHandlers.put(Action.TOGGLE_TREMOLO_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleTremoloIndependently);
		actionHandlers.put(Action.TOGGLE_VIBRATO, guitarSoundsStatusesHandler::toggleVibrato);
		actionHandlers.put(Action.TOGGLE_VIBRATO_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleVibratoIndependently);
		actionHandlers.put(Action.TOGGLE_WAVEFORM_GRAPH, waveFormDrawer::toggle);
		actionHandlers.put(Action.TOGGLE_WORD_PART, vocalsHandler::toggleWordPart);
		actionHandlers.put(Action.UNDO, undoSystem::undo);
	}

	private static final List<Action> actionsNotClearingMousePress = asList(//
			Action.FAST_BACKWARD, //
			Action.FAST_FORWARD, //
			Action.MOVE_BACKWARD, //
			Action.PREVIOUS_BEAT, //
			Action.PREVIOUS_GRID, //
			Action.PREVIOUS_ITEM, //
			Action.MOVE_FORWARD, //
			Action.SLOW_BACKWARD, //
			Action.SLOW_FORWARD);
	private static final List<Action> actionsNotStoppingAudio = asList(//
			Action.PLAY_AUDIO, //
			Action.TOGGLE_CLAPS, //
			Action.TOGGLE_METRONOME, //
			Action.TOGGLE_MIDI, //
			Action.TOGGLE_WAVEFORM_GRAPH);

	public void fireAction(final Action action) {
		if (!actionsNotClearingMousePress.contains(action)) {
			mouseHandler.cancelAllActions();
		}
		if (!actionsNotStoppingAudio.contains(action)) {
			audioHandler.stopMusic();
		}

		final Runnable actionHandler = actionHandlers.get(action);
		if (actionHandler != null) {
			actionHandler.run();
		}
	}

	private int getKeyNumber(final int code) {
		switch (code) {
			case VK_0:
			case VK_NUMPAD0:
				return 0;
			case VK_1:
			case VK_NUMPAD1:
				return 1;
			case VK_2:
			case VK_NUMPAD2:
				return 2;
			case VK_3:
			case VK_NUMPAD3:
				return 3;
			case VK_4:
			case VK_NUMPAD4:
				return 4;
			case VK_5:
			case VK_NUMPAD5:
				return 5;
			case VK_6:
			case VK_NUMPAD6:
				return 6;
			case VK_7:
			case VK_NUMPAD7:
				return 7;
			case VK_8:
			case VK_NUMPAD8:
				return 8;
			case VK_9:
			case VK_NUMPAD9:
				return 9;
			default:
				return -1;
		}
	}

	private void tryKeyNumber(final int keyCode) {
		if (modeManager.getMode() == EditMode.EMPTY) {
			return;
		}

		final int number = getKeyNumber(keyCode);
		if (number < 0) {
			return;
		}

		mouseHandler.cancelAllActions();
		audioHandler.stopMusic();

		if (ctrl) {
			toggleBookmark(number);
			return;
		}
		if (shift) {
			moveToBookmark(number);
			return;
		}

		if (modeManager.getMode() == EditMode.GUITAR) {
			handleFretNumber(number);
		}
	}

	private Action getAction() {
		return ShortcutConfig.getAction(modeManager.getMode(), new Shortcut(ctrl, shift, alt, heldNonModifierKey));
	}

	private void replaceHeldAction() {
		heldAction = getAction();
	}

	private void keyUsed(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UNDEFINED) {
			return;
		}

		if (keyCode == VK_CONTROL) {
			ctrl = true;
			replaceHeldAction();
			return;
		}
		if (keyCode == VK_SHIFT) {
			shift = true;
			replaceHeldAction();
			return;
		}
		if (keyCode == VK_ALT) {
			alt = true;
			replaceHeldAction();
			return;
		}

		heldNonModifierKey = keyCode;
		replaceHeldAction();
		if (heldAction != null) {
			fireAction(heldAction);
			return;
		}

		tryKeyNumber(keyCode);
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		keyUsed(e);
		e.consume();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final int keyCode = e.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_CONTROL:
				ctrl = false;
				replaceHeldAction();
				break;
			case KeyEvent.VK_SHIFT:
				shift = false;
				replaceHeldAction();
				break;
			case KeyEvent.VK_ALT:
				alt = false;
				replaceHeldAction();
				break;
			default:
				if (heldNonModifierKey == keyCode) {
					heldNonModifierKey = -1;
					heldAction = null;
				}
				break;
		}

		e.consume();
	}

	@Override
	public void keyTyped(final KeyEvent e) {
	}
}
