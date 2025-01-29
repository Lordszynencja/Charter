package log.charter.services;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.util.Arrays.asList;
import static log.charter.data.config.Config.frets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jcodec.common.logging.Logger;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.types.PositionType;
import log.charter.data.types.PositionWithIdAndType;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.toolbar.IChartToolbar;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.audio.AudioHandler;
import log.charter.services.audio.ClapsHandler;
import log.charter.services.audio.MetronomeHandler;
import log.charter.services.data.ChartItemsHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.GuitarSoundsHandler;
import log.charter.services.data.GuitarSoundsStatusesHandler;
import log.charter.services.data.HandShapesHandler;
import log.charter.services.data.VocalsHandler;
import log.charter.services.data.copy.CopyManager;
import log.charter.services.data.files.NewProjectCreator;
import log.charter.services.data.files.SongFileHandler;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.HighlightManager;
import log.charter.services.mouseAndKeyboard.MouseHandler;

public class ActionHandler implements Initiable {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private ChartItemsHandler chartItemsHandler;
	private ChartTimeHandler chartTimeHandler;
	private IChartToolbar chartToolbar;
	private CharterContext charterContext;
	private ClapsHandler clapsHandler;
	private CopyManager copyManager;
	private GuitarSoundsHandler guitarSoundsHandler;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private HandShapesHandler handShapesHandler;
	private HighlightManager highlightManager;
	private MetronomeHandler metronomeHandler;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private NewProjectCreator newProjectCreator;
	private RepeatManager repeatManager;
	private SelectionManager selectionManager;
	private SongFileHandler songFileHandler;
	private UndoSystem undoSystem;
	private VocalsHandler vocalsHandler;
	private WaveFormDrawer waveFormDrawer;
	private WindowedPreviewHandler windowedPreviewHandler;

	private int lastFretNumber = 0;
	private int fretNumberTimer = 0;

	private void nextArrangement() {
		switch (modeManager.getMode()) {
			case TEMPO_MAP:
				modeManager.setMode(EditMode.VOCALS);
				break;
			case VOCALS:
				modeManager.setArrangement(0);
				break;
			case GUITAR:
				if (chartData.currentArrangement >= chartData.songChart.arrangements.size() - 1) {
					modeManager.setMode(EditMode.TEMPO_MAP);
				} else {
					modeManager.setArrangement(chartData.currentArrangement + 1);
				}
				break;
			case EMPTY:
			default:
				Logger.error("Changed to next arrangement in mode " + modeManager.getMode());
				break;
		}
	}

	private void previousArrangement() {
		switch (modeManager.getMode()) {
			case TEMPO_MAP:
				modeManager.setArrangement(chartData.songChart.arrangements.size() - 1);
				break;
			case VOCALS:
				modeManager.setMode(EditMode.TEMPO_MAP);
				break;
			case GUITAR:
				if (chartData.currentArrangement <= 0) {
					modeManager.setMode(EditMode.VOCALS);
				} else {
					modeManager.setArrangement(chartData.currentArrangement - 1);
				}
				break;
			case EMPTY:
			default:
				Logger.error("Changed to next arrangement in mode " + modeManager.getMode());
				break;
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

	private void toggleAnchor() {
		final PositionWithIdAndType highlight = highlightManager.getHighlight(mouseHandler.getMouseX(),
				mouseHandler.getMouseY());
		if (!highlight.existingPosition || highlight.type != PositionType.BEAT) {
			return;
		}

		undoSystem.addUndo();
		highlight.beat.anchor = !highlight.beat.anchor;
	}

	private void toggleBookmark(final int number) {
		final Double currentBookmark = chartData.songChart.bookmarks.get(number);
		if (currentBookmark == null || currentBookmark != chartTimeHandler.time()) {
			chartData.songChart.bookmarks.put(number, chartTimeHandler.time());
		} else {
			chartData.songChart.bookmarks.remove(number);
		}
	}

	private void moveToBookmark(final int number) {
		final Double bookmark = chartData.songChart.bookmarks.get(number);
		if (bookmark == null) {
			return;
		}

		chartTimeHandler.nextTime(bookmark);
	}

	private void changeSpeed(final int change) {
		final int divideRest = Config.stretchedMusicSpeed % abs(change);
		if (divideRest != 0) {
			if (change < 0) {
				Config.stretchedMusicSpeed = max(1, min(1000, Config.stretchedMusicSpeed - divideRest));
			} else {
				Config.stretchedMusicSpeed = max(1, min(1000, Config.stretchedMusicSpeed + change - divideRest));
			}
		} else {
			Config.stretchedMusicSpeed = max(1, min(1000, Config.stretchedMusicSpeed + change));
		}

		chartToolbar.updateValues();
	}

	private final Map<Action, Runnable> actionHandlers = new HashMap<>();

	@Override
	public void init() {
		actionHandlers.put(Action.ARRANGEMENT_NEXT, this::nextArrangement);
		actionHandlers.put(Action.ARRANGEMENT_PREVIOUS, this::previousArrangement);
		actionHandlers.put(Action.COPY, copyManager::copy);
		actionHandlers.put(Action.DELETE, chartItemsHandler::delete);
		actionHandlers.put(Action.DOUBLE_GRID, this::doubleGridSize);
		actionHandlers.put(Action.EDIT_VOCALS, vocalsHandler::editVocals);
		actionHandlers.put(Action.EXIT, charterContext::exit);
		actionHandlers.put(Action.FRET_0, () -> handleFretNumber(0));
		actionHandlers.put(Action.FRET_1, () -> handleFretNumber(1));
		actionHandlers.put(Action.FRET_2, () -> handleFretNumber(2));
		actionHandlers.put(Action.FRET_3, () -> handleFretNumber(3));
		actionHandlers.put(Action.FRET_4, () -> handleFretNumber(4));
		actionHandlers.put(Action.FRET_5, () -> handleFretNumber(5));
		actionHandlers.put(Action.FRET_6, () -> handleFretNumber(6));
		actionHandlers.put(Action.FRET_7, () -> handleFretNumber(7));
		actionHandlers.put(Action.FRET_8, () -> handleFretNumber(8));
		actionHandlers.put(Action.FRET_9, () -> handleFretNumber(9));
		actionHandlers.put(Action.HALVE_GRID, this::halveGridSize);
		actionHandlers.put(Action.MARK_BOOKMARK_0, () -> toggleBookmark(0));
		actionHandlers.put(Action.MARK_BOOKMARK_1, () -> toggleBookmark(1));
		actionHandlers.put(Action.MARK_BOOKMARK_2, () -> toggleBookmark(2));
		actionHandlers.put(Action.MARK_BOOKMARK_3, () -> toggleBookmark(3));
		actionHandlers.put(Action.MARK_BOOKMARK_4, () -> toggleBookmark(4));
		actionHandlers.put(Action.MARK_BOOKMARK_5, () -> toggleBookmark(5));
		actionHandlers.put(Action.MARK_BOOKMARK_6, () -> toggleBookmark(6));
		actionHandlers.put(Action.MARK_BOOKMARK_7, () -> toggleBookmark(7));
		actionHandlers.put(Action.MARK_BOOKMARK_8, () -> toggleBookmark(8));
		actionHandlers.put(Action.MARK_BOOKMARK_9, () -> toggleBookmark(9));
		actionHandlers.put(Action.MARK_HAND_SHAPE, handShapesHandler::markHandShape);
		actionHandlers.put(Action.MOVE_STRING_DOWN, () -> guitarSoundsHandler.moveStringsWithFretChange(-1));
		actionHandlers.put(Action.MOVE_STRING_DOWN_SIMPLE, () -> guitarSoundsHandler.moveStringsWithoutFretChange(-1));
		actionHandlers.put(Action.MOVE_STRING_UP, () -> guitarSoundsHandler.moveStringsWithFretChange(1));
		actionHandlers.put(Action.MOVE_STRING_UP_SIMPLE, () -> guitarSoundsHandler.moveStringsWithoutFretChange(1));
		actionHandlers.put(Action.MOVE_FRET_DOWN, () -> guitarSoundsHandler.moveFret(-1));
		actionHandlers.put(Action.MOVE_FRET_UP, () -> guitarSoundsHandler.moveFret(1));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_0, () -> moveToBookmark(0));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_1, () -> moveToBookmark(1));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_2, () -> moveToBookmark(2));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_3, () -> moveToBookmark(3));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_4, () -> moveToBookmark(4));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_5, () -> moveToBookmark(5));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_6, () -> moveToBookmark(6));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_7, () -> moveToBookmark(7));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_8, () -> moveToBookmark(8));
		actionHandlers.put(Action.MOVE_TO_BOOKMARK_9, () -> moveToBookmark(9));
		actionHandlers.put(Action.MOVE_TO_END, chartTimeHandler::moveToEnd);
		actionHandlers.put(Action.MOVE_TO_FIRST_ITEM, chartTimeHandler::moveToFirstItem);
		actionHandlers.put(Action.MOVE_TO_LAST_ITEM, chartTimeHandler::moveToLastItem);
		actionHandlers.put(Action.MOVE_TO_START, chartTimeHandler::moveToBeginning);
		actionHandlers.put(Action.NEW_PROJECT, newProjectCreator::newSong);
		actionHandlers.put(Action.NEXT_BEAT, chartTimeHandler::moveToNextBeat);
		actionHandlers.put(Action.NEXT_GRID, chartTimeHandler::moveToNextGrid);
		actionHandlers.put(Action.NEXT_ITEM, chartTimeHandler::moveToNextItem);
		actionHandlers.put(Action.NEXT_ITEM_WITH_SELECT, chartTimeHandler::moveToNextItemWithSelect);
		actionHandlers.put(Action.OPEN_PROJECT, songFileHandler::open);
		actionHandlers.put(Action.PASTE, copyManager::paste);
		actionHandlers.put(Action.PLACE_LYRIC_FROM_TEXT, vocalsHandler::placeLyricFromText);
		actionHandlers.put(Action.PLAY_AUDIO, audioHandler::togglePlaySetSpeed);
		actionHandlers.put(Action.PREVIOUS_BEAT, chartTimeHandler::moveToPreviousBeat);
		actionHandlers.put(Action.PREVIOUS_GRID, chartTimeHandler::moveToPreviousGrid);
		actionHandlers.put(Action.PREVIOUS_ITEM, chartTimeHandler::moveToPreviousItem);
		actionHandlers.put(Action.PREVIOUS_ITEM_WITH_SELECT, chartTimeHandler::moveToPreviousItemWithSelect);
		actionHandlers.put(Action.REDO, undoSystem::redo);
		actionHandlers.put(Action.SAVE, songFileHandler::save);
		actionHandlers.put(Action.SAVE_AS, songFileHandler::saveAs);
		actionHandlers.put(Action.SELECT_ALL_NOTES, selectionManager::selectAllNotes);
		actionHandlers.put(Action.SNAP_ALL, chartItemsHandler::snapAll);
		actionHandlers.put(Action.SNAP_SELECTED, chartItemsHandler::snapSelected);
		actionHandlers.put(Action.SPECIAL_PASTE, copyManager::specialPaste);
		actionHandlers.put(Action.SPEED_DECREASE, () -> changeSpeed(-5));
		actionHandlers.put(Action.SPEED_DECREASE_FAST, () -> changeSpeed(-25));
		actionHandlers.put(Action.SPEED_DECREASE_PRECISE, () -> changeSpeed(-1));
		actionHandlers.put(Action.SPEED_INCREASE, () -> changeSpeed(5));
		actionHandlers.put(Action.SPEED_INCREASE_FAST, () -> changeSpeed(25));
		actionHandlers.put(Action.SPEED_INCREASE_PRECISE, () -> changeSpeed(1));
		actionHandlers.put(Action.TOGGLE_ACCENT, guitarSoundsStatusesHandler::toggleAccent);
		actionHandlers.put(Action.TOGGLE_ACCENT_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleAccentIndependently);
		actionHandlers.put(Action.TOGGLE_ANCHOR, this::toggleAnchor);
		actionHandlers.put(Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW,
				windowedPreviewHandler::switchBorderlessWindowedPreview);
		actionHandlers.put(Action.TOGGLE_CLAPS, clapsHandler::toggleClaps);
		actionHandlers.put(Action.TOGGLE_HARMONIC, guitarSoundsStatusesHandler::toggleHarmonic);
		actionHandlers.put(Action.TOGGLE_HARMONIC_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleHarmonicIndependently);
		actionHandlers.put(Action.TOGGLE_HOPO, guitarSoundsStatusesHandler::toggleHOPO);
		actionHandlers.put(Action.TOGGLE_HOPO_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleHOPOIndependently);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT, guitarSoundsStatusesHandler::toggleLinkNext);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleLinkNextIndependently);
		actionHandlers.put(Action.TOGGLE_METRONOME, metronomeHandler::toggleMetronome);
		actionHandlers.put(Action.TOGGLE_MIDI, audioHandler::toggleMidiNotes);
		actionHandlers.put(Action.TOGGLE_MUTE, guitarSoundsStatusesHandler::toggleMute);
		actionHandlers.put(Action.TOGGLE_MUTE_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleMuteIndependently);
		actionHandlers.put(Action.TOGGLE_PHRASE_END, vocalsHandler::togglePhraseEnd);
		actionHandlers.put(Action.TOGGLE_PREVIEW_WINDOW, windowedPreviewHandler::switchWindowedPreview);
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
			Action.PLACE_LYRIC_FROM_TEXT, //
			Action.PLAY_AUDIO, //
			Action.SPEED_DECREASE, //
			Action.SPEED_DECREASE_FAST, //
			Action.SPEED_DECREASE_PRECISE, //
			Action.SPEED_INCREASE, //
			Action.SPEED_INCREASE_FAST, //
			Action.SPEED_INCREASE_PRECISE, //
			Action.TOGGLE_CLAPS, //
			Action.TOGGLE_METRONOME, //
			Action.TOGGLE_MIDI, //
			Action.TOGGLE_WAVEFORM_GRAPH);

	public void fireAction(final Action action) {
		if (!action.editModes.contains(modeManager.getMode())) {
			return;
		}

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

	public void clearFrets() {
		lastFretNumber = 0;
	}
}
