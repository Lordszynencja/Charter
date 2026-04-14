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
import log.charter.data.config.Localization.Label;
import log.charter.data.config.ZoomUtils;
import log.charter.data.config.values.GridConfig;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.waveform.WaveFormDrawer;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.toolbar.IChartToolbar;
import log.charter.gui.components.utils.ComponentUtils;
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
import log.charter.services.data.ShowlightsHandler;
import log.charter.services.data.VocalsHandler;
import log.charter.services.data.beats.BPMDoubler;
import log.charter.services.data.beats.BPMHalver;
import log.charter.services.data.copy.CopyManager;
import log.charter.services.data.files.SongFileHandler;
import log.charter.services.data.files.newProject.NewEmptyProjectCreator;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.GuitarModeHandler;
import log.charter.services.editModes.ModeManager;
import log.charter.services.mouseAndKeyboard.KeyboardHandler;
import log.charter.services.mouseAndKeyboard.MouseHandler;

public class ActionHandler implements Initiable {
	private AudioHandler audioHandler;
	private BeatsService beatsService;
	private BPMDoubler bpmDoubler;
	private BPMHalver bpmHalver;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private ChartItemsHandler chartItemsHandler;
	private ChartTimeHandler chartTimeHandler;
	private IChartToolbar chartToolbar;
	private CharterContext charterContext;
	private ClapsHandler clapsHandler;
	private CopyManager copyManager;
	private CurrentSelectionEditor currentSelectionEditor;
	private GuitarModeHandler guitarModeHandler;
	private GuitarSoundsHandler guitarSoundsHandler;
	private GuitarSoundsStatusesHandler guitarSoundsStatusesHandler;
	private HandShapesHandler handShapesHandler;
	private KeyboardHandler keyboardHandler;
	private MetronomeHandler metronomeHandler;
	private ModeManager modeManager;
	private MouseHandler mouseHandler;
	private NewEmptyProjectCreator newEmptyProjectCreator;
	private RepeatManager repeatManager;
	private SelectionManager selectionManager;
	private ShowlightsHandler showlightsHandler;
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
			case SHOWLIGHTS -> 0;
			case VOCALS -> chartData.currentVocals;
			case GUITAR -> chartData.currentArrangement;
			default -> throw new IllegalArgumentException("Unexpected value: " + currentMode);
		};
		boolean foundNext = false;
		while (!foundNext) {
			switch (currentMode) {
				case TEMPO_MAP:
					currentMode = EditMode.SHOWLIGHTS;
					currentPath = 0;
					break;
				case SHOWLIGHTS:
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
				case SHOWLIGHTS -> true;
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
			case SHOWLIGHTS -> 0;
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
						currentMode = EditMode.SHOWLIGHTS;
					}
					break;
				case SHOWLIGHTS:
					currentMode = EditMode.TEMPO_MAP;
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
				case SHOWLIGHTS -> true;
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

	private void bookmark(final int number) {
		final Double bookmark = chartData.songChart.bookmarks.get(number);
		if (bookmark == null) {
			chartData.songChart.bookmarks.put(number, chartTimeHandler.time());
		} else if (bookmark != chartTimeHandler.time()) {
			chartTimeHandler.nextTime(bookmark);
		} else {
			chartData.songChart.bookmarks.remove(number);
		}
	}

	public void changeLength(final int change) {
		if (chartData.isEmpty) {
			return;
		}

		try {
			if (keyboardHandler.ctrl()) {
				final int zoomChange = change * (keyboardHandler.shift() ? 8 : 1);
				ZoomUtils.changeZoom(zoomChange);
				return;
			}

			if (!selectionManager.selectedAccessor().isSelected()) {
				return;
			}

			modeManager.getHandler().changeLength(change);
		} catch (final Exception ex) {
			Logger.error("Exception on length change", ex);
		}
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

	private void switchTypingPart() {
		switch (modeManager.getMode()) {
			case TEMPO_MAP -> modeManager.getTempoMapModeHandler().switchTypingPart();
			case GUITAR -> modeManager.getGuitarModeHandler().switchTypingPart();
			default -> throw new IllegalArgumentException("Unexpected value: " + modeManager.getMode());
		}
	}

	public void changeZoom(final int change) {
		ZoomUtils.changeZoom(change);
	}

	private final Map<Action, Runnable> actionHandlers = new HashMap<>();

	@Override
	public void init() {
		guitarModeHandler = modeManager.getGuitarModeHandler();

		actionHandlers.put(Action.ARRANGEMENT_NEXT, this::nextArrangement);
		actionHandlers.put(Action.ARRANGEMENT_PREVIOUS, this::previousArrangement);
		actionHandlers.put(Action.BEAT_ADD, beatsService::addBeat);
		actionHandlers.put(Action.BEAT_REMOVE, beatsService::removeBeat);
		actionHandlers.put(Action.BOOKMARK_0, () -> bookmark(0));
		actionHandlers.put(Action.BOOKMARK_1, () -> bookmark(1));
		actionHandlers.put(Action.BOOKMARK_2, () -> bookmark(2));
		actionHandlers.put(Action.BOOKMARK_3, () -> bookmark(3));
		actionHandlers.put(Action.BOOKMARK_4, () -> bookmark(4));
		actionHandlers.put(Action.BOOKMARK_5, () -> bookmark(5));
		actionHandlers.put(Action.BOOKMARK_6, () -> bookmark(6));
		actionHandlers.put(Action.BOOKMARK_7, () -> bookmark(7));
		actionHandlers.put(Action.BOOKMARK_8, () -> bookmark(8));
		actionHandlers.put(Action.BOOKMARK_9, () -> bookmark(9));
		actionHandlers.put(Action.BPM_DOUBLE, bpmDoubler::doubleBPM);
		actionHandlers.put(Action.BPM_HALVE, bpmHalver::halveBPM);
		actionHandlers.put(Action.CHANGE_GRID, chartToolbar::focusGrid);
		actionHandlers.put(Action.COPY, copyManager::copy);
		actionHandlers.put(Action.CUT, copyManager::cut);
		actionHandlers.put(Action.DECREASE_LENGTH, () -> changeLength(-1));
		actionHandlers.put(Action.DECREASE_LENGTH_FAST, () -> changeLength(-4));
		actionHandlers.put(Action.DELETE, chartItemsHandler::delete);
		actionHandlers.put(Action.DELETE_RELATED, chartItemsHandler::deleteRelated);
		actionHandlers.put(Action.DOUBLE_GRID, this::doubleGridSize);
		actionHandlers.put(Action.EDIT_VOCALS, vocalsHandler::editVocals);
		actionHandlers.put(Action.EXIT, charterContext::exit);
		actionHandlers.put(Action.FINGER_1, () -> guitarSoundsHandler.setFinger(1));
		actionHandlers.put(Action.FINGER_2, () -> guitarSoundsHandler.setFinger(2));
		actionHandlers.put(Action.FINGER_3, () -> guitarSoundsHandler.setFinger(3));
		actionHandlers.put(Action.FINGER_4, () -> guitarSoundsHandler.setFinger(4));
		actionHandlers.put(Action.FINGER_T, () -> guitarSoundsHandler.setFinger(0));
		actionHandlers.put(Action.HALVE_GRID, this::halveGridSize);
		actionHandlers.put(Action.INCREASE_LENGTH, () -> changeLength(1));
		actionHandlers.put(Action.INCREASE_LENGTH_FAST, () -> changeLength(4));
		actionHandlers.put(Action.INSERT_EVENT_POINT, guitarModeHandler::insertEventPoint);
		actionHandlers.put(Action.INSERT_FHP, guitarModeHandler::insertFHP);
		actionHandlers.put(Action.INSERT_HAND_SHAPE, guitarModeHandler::insertHandShape);
		actionHandlers.put(Action.INSERT_SHOWLIGHT, showlightsHandler::insertShowlight);
		actionHandlers.put(Action.INSERT_TONE_CHANGE, guitarModeHandler::insertToneChange);
		actionHandlers.put(Action.INSERT_VOCAL, vocalsHandler::insertVocal);
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
		actionHandlers.put(Action.MOVE_TO_END, chartTimeHandler::moveToEnd);
		actionHandlers.put(Action.MOVE_TO_FIRST_ITEM, chartTimeHandler::moveToFirstItem);
		actionHandlers.put(Action.MOVE_TO_LAST_ITEM, chartTimeHandler::moveToLastItem);
		actionHandlers.put(Action.MOVE_TO_START, chartTimeHandler::moveToBeginning);
		actionHandlers.put(Action.NEW_PROJECT, newEmptyProjectCreator::newProject);
		actionHandlers.put(Action.NEXT_BEAT, chartTimeHandler::moveToNextBeat);
		actionHandlers.put(Action.NEXT_GRID_POSITION, chartTimeHandler::moveToNextGrid);
		actionHandlers.put(Action.NEXT_ITEM, chartTimeHandler::moveToNextItem);
		actionHandlers.put(Action.NEXT_ITEM_TYPE, selectionManager::nextItemType);
		actionHandlers.put(Action.NEXT_ITEM_WITH_SELECT, () -> chartTimeHandler.moveToNextItemWithSelect(false, false));
		actionHandlers.put(Action.NEXT_ITEM_WITH_SELECT_CTRL,
				() -> chartTimeHandler.moveToNextItemWithSelect(true, false));
		actionHandlers.put(Action.NEXT_ITEM_WITH_SELECT_CTRL_SHIFT,
				() -> chartTimeHandler.moveToNextItemWithSelect(true, true));
		actionHandlers.put(Action.NEXT_ITEM_WITH_SELECT_SHIFT,
				() -> chartTimeHandler.moveToNextItemWithSelect(false, true));
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
		actionHandlers.put(Action.OPEN_PROJECT, songFileHandler::open);
		actionHandlers.put(Action.PASTE, copyManager::paste);
		actionHandlers.put(Action.PLACE_LYRIC_FROM_TEXT, vocalsHandler::placeLyricFromText);
		actionHandlers.put(Action.PLAY_AUDIO, audioHandler::togglePlaySetSpeed);
		actionHandlers.put(Action.PREVIOUS_BEAT, chartTimeHandler::moveToPreviousBeat);
		actionHandlers.put(Action.PREVIOUS_GRID_POSITION, chartTimeHandler::moveToPreviousGrid);
		actionHandlers.put(Action.PREVIOUS_ITEM, chartTimeHandler::moveToPreviousItem);
		actionHandlers.put(Action.PREVIOUS_ITEM_TYPE, selectionManager::previousItemType);
		actionHandlers.put(Action.PREVIOUS_ITEM_WITH_SELECT,
				() -> chartTimeHandler.moveToPreviousItemWithSelect(false, false));
		actionHandlers.put(Action.PREVIOUS_ITEM_WITH_SELECT_CTRL,
				() -> chartTimeHandler.moveToPreviousItemWithSelect(true, false));
		actionHandlers.put(Action.PREVIOUS_ITEM_WITH_SELECT_CTRL_SHIFT,
				() -> chartTimeHandler.moveToPreviousItemWithSelect(true, true));
		actionHandlers.put(Action.PREVIOUS_ITEM_WITH_SELECT_SHIFT,
				() -> chartTimeHandler.moveToPreviousItemWithSelect(false, true));
		actionHandlers.put(Action.REDO, undoSystem::redo);
		actionHandlers.put(Action.SAVE_PROJECT, songFileHandler::save);
		actionHandlers.put(Action.SAVE_PROJECT_AS, songFileHandler::saveAs);
		actionHandlers.put(Action.SELECT_ALL, selectionManager::selectAll);
		actionHandlers.put(Action.SET_HAND_SHAPE_TEMPLATE_ON_CHORDS, guitarSoundsHandler::setHandShapeTemplateOnChords);
		actionHandlers.put(Action.SNAP_ALL, chartItemsHandler::snapAll);
		actionHandlers.put(Action.SNAP_SELECTED, chartItemsHandler::snapSelected);
		actionHandlers.put(Action.SPECIAL_PASTE, copyManager::specialPaste);
		actionHandlers.put(Action.SPEED_DECREASE, () -> changeSpeed(-5));
		actionHandlers.put(Action.SPEED_DECREASE_FAST, () -> changeSpeed(-25));
		actionHandlers.put(Action.SPEED_DECREASE_PRECISE, () -> changeSpeed(-1));
		actionHandlers.put(Action.SPEED_INCREASE, () -> changeSpeed(5));
		actionHandlers.put(Action.SPEED_INCREASE_FAST, () -> changeSpeed(25));
		actionHandlers.put(Action.SPEED_INCREASE_PRECISE, () -> changeSpeed(1));
		actionHandlers.put(Action.STRING_1, () -> currentSelectionEditor.toggleString(0));
		actionHandlers.put(Action.STRING_2, () -> currentSelectionEditor.toggleString(1));
		actionHandlers.put(Action.STRING_3, () -> currentSelectionEditor.toggleString(2));
		actionHandlers.put(Action.STRING_4, () -> currentSelectionEditor.toggleString(3));
		actionHandlers.put(Action.STRING_5, () -> currentSelectionEditor.toggleString(4));
		actionHandlers.put(Action.STRING_6, () -> currentSelectionEditor.toggleString(5));
		actionHandlers.put(Action.STRING_7, () -> currentSelectionEditor.toggleString(6));
		actionHandlers.put(Action.STRING_8, () -> currentSelectionEditor.toggleString(7));
		actionHandlers.put(Action.STRING_9, () -> currentSelectionEditor.toggleString(8));
		actionHandlers.put(Action.SWITCH_TYPING_PART, this::switchTypingPart);
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
		actionHandlers.put(Action.TOGGLE_IGNORE, guitarSoundsStatusesHandler::toggleIgnore);
		actionHandlers.put(Action.TOGGLE_IGNORE_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleIgnoreIndependently);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT, guitarSoundsStatusesHandler::toggleLinkNext);
		actionHandlers.put(Action.TOGGLE_LINK_NEXT_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleLinkNextIndependently);
		actionHandlers.put(Action.TOGGLE_LOW_PASS_FILTER, audioHandler::toggleLowPassFilter);
		actionHandlers.put(Action.TOGGLE_METRONOME, metronomeHandler::toggleMetronome);
		actionHandlers.put(Action.TOGGLE_MIDI, audioHandler::toggleMidiNotes);
		actionHandlers.put(Action.TOGGLE_MUTE, guitarSoundsStatusesHandler::toggleMute);
		actionHandlers.put(Action.TOGGLE_MUTE_INDEPENDENTLY, guitarSoundsStatusesHandler::toggleMuteIndependently);
		for (int i = 0; i < 9; i++) {
			final int string = i;
			final Action action = Action.valueOf("TOGGLE_NOTE_" + (string + 1));
			actionHandlers.put(action, () -> guitarModeHandler.toggleString(string));
		}
		actionHandlers.put(Action.TOGGLE_ONLY_BOX, () -> guitarSoundsStatusesHandler.toggleOnlyBox(false));
		actionHandlers.put(Action.TOGGLE_ONLY_BOX_INDEPENDENTLY, () -> guitarSoundsStatusesHandler.toggleOnlyBox(true));
		actionHandlers.put(Action.TOGGLE_PASS_NOTES, guitarSoundsStatusesHandler::togglePassNotes);
		actionHandlers.put(Action.TOGGLE_PASS_NOTES_INDEPENDENTLY,
				guitarSoundsStatusesHandler::togglePassNotesIndependently);
		actionHandlers.put(Action.TOGGLE_PHRASE_END, vocalsHandler::togglePhraseEnd);
		actionHandlers.put(Action.TOGGLE_SLAP_POP, guitarSoundsStatusesHandler::toggleSlapPop);
		actionHandlers.put(Action.TOGGLE_SLAP_POP_INDEPENDENTLY,
				guitarSoundsStatusesHandler::toggleSlapPopIndependently);
		actionHandlers.put(Action.TOGGLE_SPLIT, () -> guitarSoundsStatusesHandler.toggleSplit(false));
		actionHandlers.put(Action.TOGGLE_SPLIT_INDEPENDENTLY, () -> guitarSoundsStatusesHandler.toggleSplit(true));
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
		actionHandlers.put(Action.ZOOM_IN, () -> changeZoom(1));
		actionHandlers.put(Action.ZOOM_IN_FAST, () -> changeZoom(16));
		actionHandlers.put(Action.ZOOM_OUT, () -> changeZoom(-1));
		actionHandlers.put(Action.ZOOM_OUT_FAST, () -> changeZoom(-16));
	}

	private static final List<Action> actionsNotClearingMousePress = asList(//
			Action.FAST_BACKWARD, //
			Action.FAST_FORWARD, //
			Action.MOVE_BACKWARD, //
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

				if (modeManager.getMode() == EditMode.SHOWLIGHTS) {
					chartData.songChart.refreshShowlightsLists();
				}
			}

		} catch (final Exception ex) {
			Logger.error("Exception on action " + action, ex);
			ComponentUtils.showPopup(charterFrame, Label.ERROR, ex.getLocalizedMessage());
		}
	}

	public void clearNumbers() {
		modeManager.clearNumbers();
	}
}
