package log.charter.data.managers;

import java.util.HashMap;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.data.managers.CharterContext.Initiable;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.modes.EmptyModeHandler;
import log.charter.data.managers.modes.GuitarModeHandler;
import log.charter.data.managers.modes.ModeHandler;
import log.charter.data.managers.modes.TempoMapModeHandler;
import log.charter.data.managers.modes.VocalModeHandler;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.menuHandlers.CharterMenuBar;

public class ModeManager implements Initiable {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private CharterContext charterContext;
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

	@Override
	public void init() {
		charterContext.initObject(guitarModeHandler);
		charterContext.initObject(tempoMapModeHandler);
		charterContext.initObject(vocalModeHandler);
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
		charterFrame.updateSizes();
	}

	public void setArrangement(final int arrangementId) {
		chartData.currentArrangement = arrangementId;
		setLevel(0);
		setMode(EditMode.GUITAR);

		charterFrame.updateSizes();
	}

	public void setLevel(final int level) {
		chartData.currentLevel = level;
	}

	public EditMode getMode() {
		return editMode;
	}
}
