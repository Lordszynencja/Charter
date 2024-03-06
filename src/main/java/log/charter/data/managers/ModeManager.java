package log.charter.data.managers;

import java.util.HashMap;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.modes.EmptyModeHandler;
import log.charter.data.managers.modes.GuitarModeHandler;
import log.charter.data.managers.modes.ModeHandler;
import log.charter.data.managers.modes.TempoMapModeHandler;
import log.charter.data.managers.modes.VocalModeHandler;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.tabs.selectionEditor.CurrentSelectionEditor;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.data.ChartTimeHandler;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;

public class ModeManager {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ChartToolbar chartToolbar;
	private SelectionManager selectionManager;

	private EditMode editMode = EditMode.EMPTY;

	private final EmptyModeHandler emptyModeHandler = new EmptyModeHandler();
	private final GuitarModeHandler guitarModeHandler = new GuitarModeHandler();
	private final TempoMapModeHandler tempoMapModeHandler = new TempoMapModeHandler();
	private final VocalModeHandler vocalModeHandler = new VocalModeHandler();

	private final Map<EditMode, ModeHandler> modeHandlers = new HashMap<>();

	public ModeManager() {
		modeHandlers.put(EditMode.EMPTY, emptyModeHandler);
		modeHandlers.put(EditMode.GUITAR, guitarModeHandler);
		modeHandlers.put(EditMode.TEMPO_MAP, tempoMapModeHandler);
		modeHandlers.put(EditMode.VOCALS, vocalModeHandler);
	}

	public void init(final AudioHandler audioHandler, final CharterMenuBar charterMenuBar,
			final ChartTimeHandler chartTimeHandler, final ChartToolbar chartToolbar,
			final CurrentSelectionEditor currentSelectionEditor, final ChartData chartData,
			final CharterFrame charterFrame, final HighlightManager highlightManager,
			final KeyboardHandler keyboardHandler, final SelectionManager selectionManager,
			final UndoSystem undoSystem) {
		this.audioHandler = audioHandler;
		this.chartData = chartData;
		this.charterFrame = charterFrame;
		this.charterMenuBar = charterMenuBar;
		this.chartToolbar = chartToolbar;
		this.selectionManager = selectionManager;

		emptyModeHandler.init();
		guitarModeHandler.init(currentSelectionEditor, chartData, charterFrame, highlightManager, keyboardHandler,
				selectionManager, undoSystem);
		tempoMapModeHandler.init(chartTimeHandler, chartData, charterFrame, undoSystem);
		vocalModeHandler.init(chartData, charterFrame, currentSelectionEditor, keyboardHandler, selectionManager,
				undoSystem);
	}

	public ModeHandler getHandler() {
		return modeHandlers.get(editMode);
	}

	public void setMode(final EditMode editMode) {
		if (editMode == this.editMode) {
			return;
		}

		audioHandler.stopMusic();
		selectionManager.clear();

		this.editMode = editMode;

		chartToolbar.updateValues();
		charterMenuBar.refreshMenus();
		charterFrame.updateEditAreaSizes();
	}

	public void setArrangement(final int arrangementId) {
		chartData.currentArrangement = arrangementId;
		setLevel(0);
		setMode(EditMode.GUITAR);

		charterFrame.updateEditAreaSizes();
	}

	public void setLevel(final int level) {
		chartData.currentLevel = level;
	}

	public EditMode getMode() {
		return editMode;
	}
}
