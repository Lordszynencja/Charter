package log.charter.services;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.values.GridConfig;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.toolbar.IChartToolbar;
import log.charter.io.Logger;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.audio.AudioHandler;
import log.charter.services.audio.ClapsHandler;
import log.charter.services.audio.MetronomeHandler;
import log.charter.services.data.BeatsService;
import log.charter.services.data.ChartItemsHandler;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.GuitarSoundsHandler;
import log.charter.services.data.GuitarSoundsStatusesHandler;
import log.charter.services.data.HandShapesHandler;
import log.charter.services.data.VocalsHandler;
import log.charter.services.data.beats.BPMDoubler;
import log.charter.services.data.beats.BPMHalver;
import log.charter.services.data.copy.CopyManager;
import log.charter.services.data.files.SongFileHandler;
import log.charter.services.data.files.newProject.NewEmptyProjectCreator;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.MouseHandler;

public class ActionHandler implements Initiable {
	private AudioHandler audioHandler;
	private BeatsService beatsService;
	private BPMDoubler bpmDoubler;
	private BPMHalver bpmHalver;
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
	private MetronomeHandler metronomeHandler;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private NewEmptyProjectCreator newEmptyProjectCreator;
	private RepeatManager repeatManager;
	private SelectionManager selectionManager;
	private SongFileHandler songFileHandler;
	private UndoSystem undoSystem;
	private VocalsHandler vocalsHandler;
	private WaveFormDrawer waveFormDrawer;
	private WindowedPreviewHandler windowedPreviewHandler;

	private void switchTo(final EditMode editMode, final int id) {
		switch (editMode) {
			case VOCALS:
				modeManager.setVocalPath(id);
				break;
			case GUITAR:
				modeManager.setArrangement(id);
				break;
			default:
				modeManager.setMode(editMode);
				break;
		}
	}

	private void nextArrangement() {
		EditMode currentMode = modeManager.getMode();
		if (currentMode == EditMode.EMPTY) {
			return;
		}

		int currentPath = switch (currentMode) {
			case TEMPO_MAP -> 0;
			case VOCALS -> chartData.currentVocals;
			case GUITAR -> chartData.currentArrangement;
			default -> throw new IllegalArgumentException("Unexpected value: " + currentMode);
		};
		boolean foundNext = false;
		while (!foundNext) {
			switch (currentMode) {
				case TEMPO_MAP:
					currentMode = EditMode.VOCALS;
					currentPath = 0;
					break;
				case VOCALS:
					currentPath++;
					if (currentPath >= chartData.songChart.vocalPaths.size()) {
						currentMode = EditMode.GUITAR;
						currentPath = 0;
					}
					break;
				case GUITAR:
					currentPath++;
					if (currentPath >= chartData.songChart.arrangements.size()) {
						currentMode = EditMode.TEMPO_MAP;
						currentPath = 0;
					}
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + currentMode);
			}

			foundNext = switch (currentMode) {
				case TEMPO_MAP -> true;
				case VOCALS -> currentPath < chartData.songChart.vocalPaths.size();
				case GUITAR -> currentPath < chartData.songChart.arrangements.size();
				default -> throw new IllegalArgumentException("Unexpected value: " + currentMode);
			};
		}

		switchTo(currentMode, currentPath);
	}

	private void previousArrangement() {
		EditMode currentMode = modeManager.getMode();
		if (currentMode == EditMode.EMPTY) {
			return;
		}

		int currentPath = switch (currentMode) {
			case TEMPO_MAP -> 0;
			case VOCALS -> chartData.currentVocals;
			case GUITAR -> chartData.currentArrangement;
			default -> throw new IllegalArgumentException("Unexpected value: " + currentMode);
		};
		boolean foundNext = false;
		while (!foundNext) {
			switch (currentMode) {
				case GUITAR:
					currentPath--;
					if (currentPath < 0) {
						currentMode = EditMode.VOCALS;
						currentPath = chartData.songChart.vocalPaths.size() - 1;
					}
					break;
				case VOCALS:
					currentPath--;
					if (currentPath < 0) {
						currentMode = EditMode.TEMPO_MAP;
					}
					break;
				case TEMPO_MAP:
					currentMode = EditMode.GUITAR;
					currentPath = chartData.songChart.arrangements.size() - 1;
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + currentMode);
			}

			foundNext = switch (currentMode) {
				case TEMPO_MAP -> true;
				case VOCALS -> currentPath >= 0;
				case GUITAR -> currentPath >= 0;
				default -> throw new IllegalArgumentException("Unexpected value: " + currentMode);
			};
		}

		switchTo(currentMode, currentPath);
	}

	private void handleNumber(final int number) {
		modeManager.getHandler().handleNumber(number);
	}

	private void doubleGridSize() {
		if (GridConfig.gridSize <= 512) {
			GridConfig.gridSize *= 2;
			Config.markChanged();

			chartToolbar.updateValues();
		}
	}

	private void halveGridSize() {
		if (GridConfig.gridSize % 2 == 0) {
			GridConfig.gridSize /= 2;
			Config.markChanged();

			chartToolbar.updateValues();
		}
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
		actionHandlers.put(Action.BEAT_ADD, beatsService::addBeat);
		actionHandlers.put(Action.BEAT_REMOVE, beatsService::removeBeat);
		actionHandlers.put(Action.BPM_DOUBLE, bpmDoubler::doubleBPM);
		actionHandlers.put(Action.BPM_HALVE, bpmHalver::halveBPM);
		actionHandlers.put(Action.COPY, copyManager::copy);
		actionHandlers.put(Action.DELETE, chartItemsHandler::delete);
		actionHandlers.put(Action.DOUBLE_GRID, this::doubleGridSize);
		actionHandlers.put(Action.EDIT_VOCALS, vocalsHandler::editVocals);
		actionHandlers.put(Action.EXIT, charterContext::exit);
		actionHandlers.put(Action.NUMBER_0, () -> handleNumber(0));
		actionHandlers.put(Action.NUMBER_1, () -> handleNumber(1));
		actionHandlers.put(Action.NUMBER_2, () -> handleNumber(2));
		actionHandlers.put(Action.NUMBER_3, () -> handleNumber(3));
		actionHandlers.put(Action.NUMBER_4, () -> handleNumber(4));
		actionHandlers.put(Action.NUMBER_5, () -> handleNumber(5));
		actionHandlers.put(Action.NUMBER_6, () -> handleNumber(6));
		actionHandlers.put(Action.NUMBER_7, () -> handleNumber(7));
		actionHandlers.put(Action.NUMBER_8, () -> handleNumber(8));
		actionHandlers.put(Action.NUMBER_9, () -> handleNumber(9));
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
		actionHandlers.put(Action.MEASURE_ADD, beatsService::addMeasure);
		actionHandlers.put(Action.MEASURE_REMOVE, beatsService::removeMeasure);
		actionHandlers.put(Action.MOVE_STRING_DOWN, () -> guitarSoundsHandler.moveStringsWithFretChange(-1));
		actionHandlers.put(Action.MOVE_STRING_DOWN_SIMPLE, () -> guitarSoundsHandler.moveStringsWithoutFretChange(-1));
		actionHandlers.put(Action.MOVE_STRING_UP, () -> guitarSoundsHandler.moveStringsWithFretChange(1));
		actionHandlers.put(Action.MOVE_STRING_UP_SIMPLE, () -> guitarSoundsHandler.moveStringsWithoutFretChange(1));
		actionHandlers.put(Action.MOVE_FRET_DOWN, () -> guitarSoundsHandler.moveFret(-1));
		actionHandlers.put(Action.MOVE_FRET_DOWN_OCTAVE, () -> guitarSoundsHandler.moveFret(-12));
		actionHandlers.put(Action.MOVE_FRET_UP, () -> guitarSoundsHandler.moveFret(1));
		actionHandlers.put(Action.MOVE_FRET_UP_OCTAVE, () -> guitarSoundsHandler.moveFret(12));
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
		actionHandlers.put(Action.NEW_PROJECT, newEmptyProjectCreator::newProject);
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
		actionHandlers.put(Action.SELECT_ALL, selectionManager::selectAll);
		actionHandlers.put(Action.SNAP_ALL, chartItemsHandler::snapAll);
		actionHandlers.put(Action.SNAP_SELECTED, chartItemsHandler::snapSelected);
		actionHandlers.put(Action.SPECIAL_PASTE, copyManager::specialPaste);
		actionHandlers.put(Action.SPEED_DECREASE, () -> changeSpeed(-5));
		actionHandlers.put(Action.SPEED_DECREASE_FAST, () -> changeSpeed(-25));
		actionHandlers.put(Action.SPEED_DECREASE_PRECISE, () -> changeSpeed(-1));
		actionHandlers.put(Action.SPEED_INCREASE, () -> changeSpeed(5));
		actionHandlers.put(Action.SPEED_INCREASE_FAST, () -> changeSpeed(25));
		actionHandlers.put(Action.SPEED_INCREASE_PRECISE, () -> changeSpeed(1));
		actionHandlers.put(Action.SWITCH_TS_TYPING_PART,
				() -> modeManager.getTempoMapModeHandler().switchTSTypingPart());
		actionHandlers.put(Action.TOGGLE_ACCENT, guitarSoundsStatusesHandler::toggleAccent);
		actionHandlers.put(Action.TOGGLE_ACCENT_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleAccentIndependently);
		actionHandlers.put(Action.TOGGLE_ANCHOR, beatsService::toggleAnchor);
		actionHandlers.put(Action.TOGGLE_BAND_PASS_FILTER, audioHandler::toggleBandPassFilter);
		actionHandlers.put(Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW,
				windowedPreviewHandler::switchBorderlessWindowedPreview);
		actionHandlers.put(Action.TOGGLE_CLAPS, clapsHandler::toggleClaps);
		actionHandlers.put(Action.TOGGLE_HARMONIC, guitarSoundsStatusesHandler::toggleHarmonic);
		actionHandlers.put(Action.TOGGLE_HARMONIC_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleHarmonicIndependently);
		actionHandlers.put(Action.TOGGLE_HIGH_PASS_FILTER, audioHandler::toggleHighPassFilter);
		actionHandlers.put(Action.TOGGLE_HOPO, guitarSoundsStatusesHandler::toggleHOPO);
		actionHandlers.put(Action.TOGGLE_HOPO_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleHOPOIndependently);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT, guitarSoundsStatusesHandler::toggleLinkNext);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleLinkNextIndependently);
		actionHandlers.put(Action.TOGGLE_LOW_PASS_FILTER, audioHandler::toggleLowPassFilter);
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
		try {
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

		} catch (final Exception ex) {
			Logger.error("Exception on action " + action, ex);
		}
	}

	public void clearNumbers() {
		modeManager.clearNumbers();
	}
}
