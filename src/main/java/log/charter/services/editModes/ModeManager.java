package log.charter.services.editModes;

import java.util.HashMap;
import java.util.Map;

import log.charter.data.ChartData;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.simple.ChartMap;
import log.charter.gui.components.tabs.chordEditor.ChordTemplatesEditorTab;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.gui.components.utils.TitleUpdater;
import log.charter.gui.menuHandlers.CharterMenuBar;
import log.charter.services.CharterContext;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.audio.AudioHandler;
import log.charter.services.data.selection.SelectionManager;

public class ModeManager implements Initiable {
	private AudioHandler audioHandler;
	private ChartData chartData;
	private ChartMap chartMap;
	private CharterContext charterContext;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ChartToolbar chartToolbar;
	private ChordTemplatesEditorTab chordTemplatesEditorTab;
	private SelectionManager selectionManager;
	private TitleUpdater titleUpdater;

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
		titleUpdater.updateTitle();
		chartMap.triggerRedraw();
		chordTemplatesEditorTab.refreshTemplates();
	}

	public void setVocalPath(final int vocalPathId) {
		if (vocalPathId == chartData.currentVocals && editMode == EditMode.VOCALS) {
			return;
		}

		audioHandler.stopMusic();
		selectionManager.clear();

		chartData.currentVocals = vocalPathId;
		chartData.currentArrangement = -1;
		setLevel(0);
		setMode(EditMode.VOCALS);

		chartToolbar.updateValues();
		charterMenuBar.refreshMenus();
		charterFrame.updateSizes();
		titleUpdater.updateTitle();
		chartMap.triggerRedraw();
		chordTemplatesEditorTab.refreshTemplates();
	}

	public void setArrangement(final int arrangementId) {
		if (arrangementId == chartData.currentArrangement && editMode == EditMode.GUITAR) {
			return;
		}

		audioHandler.stopMusic();
		selectionManager.clear();

		chartData.currentArrangement = arrangementId;
		setLevel(0);
		setMode(EditMode.GUITAR);

		chartToolbar.updateValues();
		charterMenuBar.refreshMenus();
		charterFrame.updateSizes();
		titleUpdater.updateTitle();
		chartMap.triggerRedraw();
		chordTemplatesEditorTab.refreshTemplates();
	}

	public void setLevel(final int level) {
		chartData.currentLevel = level;
	}

	public EditMode getMode() {
		return editMode;
	}

	public void clearNumbers() {
		modeHandlers.values().forEach(ModeHandler::clearNumbers);
	}

	public TempoMapModeHandler getTempoMapModeHandler() {
		return tempoMapModeHandler;
	}
}
