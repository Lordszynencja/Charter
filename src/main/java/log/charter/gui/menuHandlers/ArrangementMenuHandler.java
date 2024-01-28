package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.AudioDrawer;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.panes.ArrangementSettingsPane;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.song.ArrangementChart;
import log.charter.util.CollectionUtils.HashMap2;

class ArrangementMenuHandler extends CharterMenuHandler {
	private AudioDrawer audioDrawer;
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	public void init(final AudioDrawer audioDrawer, final AudioHandler audioHandler, final ChartData data,
			final CharterFrame frame, final CharterMenuBar charterMenuBar, final ModeManager modeManager,
			final SelectionManager selectionManager) {
		this.audioDrawer = audioDrawer;
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.charterMenuBar = charterMenuBar;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty;
	}

	private void addArrangementsList(final JMenu menu) {
		final HashMap2<String, Integer> arrangementNumbers = new HashMap2<>();
		for (int i = 0; i < data.songChart.arrangements.size(); i++) {
			final ArrangementChart arrangement = data.songChart.arrangements.get(i);
			String arrangementName = arrangement.getTypeNameLabel();
			final int arrangementNumber = arrangementNumbers.getOrDefault(arrangementName, 1);
			arrangementNumbers.put(arrangementName, arrangementNumber + 1);
			if (arrangementNumber > 1) {
				arrangementName += " " + arrangementNumber;
			}

			final int arrangementId = i;
			menu.add(createItem(arrangementName, () -> changeArrangement(arrangementId)));
		}
	}

	private void createLevelMenuItems(final JMenu menu) {
		for (final int level : data.getCurrentArrangement().levels.keySet()) {
			menu.add(createItem("Level " + level, () -> changeLevel(level)));
		}
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.ARRANGEMENT_MENU.label());

		menu.add(createItem(EditMode.TEMPO_MAP.label, () -> changeEditMode(EditMode.TEMPO_MAP)));
		menu.add(createItem(EditMode.VOCALS.label, () -> changeEditMode(EditMode.VOCALS)));
		addArrangementsList(menu);
		menu.add(createItem("New arrangement...", this::addArrangement));

		if (modeManager.editMode == EditMode.GUITAR) {
			menu.addSeparator();
			menu.add(createItem(Label.ARRANGEMENT_MENU_OPTIONS, this::editOptions));

			menu.addSeparator();
			createLevelMenuItems(menu);
		}

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_MIDI_NOTES, "F2", audioHandler::toggleMidiNotes));
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_CLAPS, "F3", audioHandler::toggleClaps));
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_METRONOME, "F4", audioHandler::toggleMetronome));
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_WAVEFORM, "F5", audioDrawer::toggle));

		return menu;
	}

	private void changeEditMode(final EditMode editMode) {
		audioHandler.stopMusic();
		selectionManager.clear();
		modeManager.editMode = editMode;

		charterMenuBar.refreshMenus();

		frame.updateEditAreaSizes();
	}

	private void changeArrangement(final int arrangementId) {
		data.currentArrangement = arrangementId;
		data.changeDifficulty(0);
		changeEditMode(EditMode.GUITAR);

		frame.updateEditAreaSizes();
	}

	private void addArrangement() {
		final int previousArrangement = data.currentArrangement;
		final EditMode previousEditMode = modeManager.editMode;
		final int previousDifficulty = data.currentLevel;
		data.currentArrangement = data.songChart.arrangements.size();
		data.changeDifficulty(0);
		data.songChart.arrangements.add(new ArrangementChart(ArrangementType.Lead, data.songChart.beatsMap.beats));
		changeEditMode(EditMode.GUITAR);

		new ArrangementSettingsPane(charterMenuBar, data, frame, selectionManager, () -> {
			data.currentArrangement = previousArrangement;
			data.changeDifficulty(previousDifficulty);
			data.songChart.arrangements.remove(data.songChart.arrangements.size() - 1);

			changeEditMode(previousEditMode);
			charterMenuBar.refreshMenus();
		}, true);
	}

	private void editOptions() {
		new ArrangementSettingsPane(charterMenuBar, data, frame, selectionManager, null, false);
	}

	private void changeLevel(final int levelId) {
		data.currentLevel = levelId;
		changeEditMode(EditMode.GUITAR);
	}

}
