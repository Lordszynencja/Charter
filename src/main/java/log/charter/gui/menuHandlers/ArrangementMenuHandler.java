package log.charter.gui.menuHandlers;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.ModeManager;
import log.charter.data.managers.modes.EditMode;
import log.charter.data.managers.selection.SelectionManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.chartPanelDrawers.common.WaveFormDrawer;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.panes.songSettings.ArrangementSettingsPane;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.song.Arrangement;
import log.charter.util.CollectionUtils.HashMap2;

public class ArrangementMenuHandler extends CharterMenuHandler {
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SelectionManager selectionManager;
	private WaveFormDrawer waveFormDrawer;

	public void init(final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final CharterMenuBar charterMenuBar, final ModeManager modeManager, final SelectionManager selectionManager,
			final WaveFormDrawer waveFormDrawer) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.charterMenuBar = charterMenuBar;
		this.modeManager = modeManager;
		this.selectionManager = selectionManager;
		this.waveFormDrawer = waveFormDrawer;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty;
	}

	private void addArrangementsList(final JMenu menu) {
		final HashMap2<String, Integer> arrangementNumbers = new HashMap2<>();
		for (int i = 0; i < data.songChart.arrangements.size(); i++) {
			final Arrangement arrangement = data.songChart.arrangements.get(i);
			String arrangementName = arrangement.getTypeNameLabel();
			final int arrangementNumber = arrangementNumbers.getOrDefault(arrangementName, 1);
			arrangementNumbers.put(arrangementName, arrangementNumber + 1);
			if (arrangementNumber > 1) {
				arrangementName += " " + arrangementNumber;
			}

			final int arrangementId = i;
			menu.add(createItem(arrangementName, () -> modeManager.setArrangement(arrangementId)));
		}
	}

	private void createLevelMenuItems(final JMenu menu) {
		for (int level = 0; level < data.getCurrentArrangement().levels.size(); level++) {
			final int levelToChangeTo = level;
			menu.add(createItem("Level " + level, () -> modeManager.setLevel(levelToChangeTo)));
		}
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.ARRANGEMENT_MENU.label());

		menu.add(createItem(Label.ARRANGEMENT_MENU_TEMPO_MAP, () -> modeManager.setMode(EditMode.TEMPO_MAP)));
		menu.add(createItem(Label.ARRANGEMENT_MENU_VOCALS, () -> modeManager.setMode(EditMode.VOCALS)));
		addArrangementsList(menu);
		menu.add(createItem("New arrangement...", this::addArrangement));

		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.addSeparator();
			menu.add(createItem(Label.ARRANGEMENT_MENU_OPTIONS, this::editOptions));

			menu.addSeparator();
			createLevelMenuItems(menu);
		}

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_MIDI_NOTES, "F2", audioHandler::toggleMidiNotes));
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_CLAPS, "F3", audioHandler::toggleClaps));
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_METRONOME, "F4", audioHandler::toggleMetronome));
		menu.add(new SpecialMenuItem(Label.ARRANGEMENT_MENU_TOGGLE_WAVEFORM, "F5", waveFormDrawer::toggle));

		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.addSeparator();
			menu.add(createItem(Label.ARRANGEMENT_MENU_DELETE_ARRANGEMENT, this::deleteArrangement));
		}

		return menu;
	}

	private void addArrangement() {
		final int previousArrangement = data.currentArrangement;
		final EditMode previousEditMode = modeManager.getMode();
		final int previousDifficulty = data.currentLevel;
		data.currentArrangement = data.songChart.arrangements.size();
		data.songChart.arrangements.add(new Arrangement(ArrangementType.Lead, data.songChart.beatsMap.beats));
		modeManager.setArrangement(data.songChart.arrangements.size() - 1);

		new ArrangementSettingsPane(charterMenuBar, data, frame, selectionManager, () -> {
			if (previousEditMode == EditMode.GUITAR) {
				modeManager.setArrangement(previousArrangement);
				modeManager.setLevel(previousDifficulty);
			} else {
				modeManager.setMode(previousEditMode);
			}

			data.songChart.arrangements.remove(data.songChart.arrangements.size() - 1);

			charterMenuBar.refreshMenus();
		}, true);
	}

	private void editOptions() {
		new ArrangementSettingsPane(charterMenuBar, data, frame, selectionManager, null, false);
	}

	private void deleteArrangement() {
		final String arrangementName = "[" + (data.currentArrangement + 1) + "] "
				+ data.getCurrentArrangement().getTypeNameLabel();
		final String msg = Label.DELETE_ARRANGEMENT_MSG.label().formatted(arrangementName);
		final int option = JOptionPane.showConfirmDialog(frame, msg, Label.DELETE_ARRANGEMENT_TITLE.label(),
				JOptionPane.YES_NO_OPTION);

		if (option != JOptionPane.YES_OPTION) {
			return;
		}

		final int arrangementToRemove = data.currentArrangement;
		data.songChart.arrangements.remove(arrangementToRemove);

		if (data.songChart.arrangements.size() > 0) {
			if (data.songChart.arrangements.size() == arrangementToRemove) {
				modeManager.setArrangement(arrangementToRemove - 1);
			} else {
				modeManager.setArrangement(arrangementToRemove);
			}
		} else {
			modeManager.setMode(EditMode.VOCALS);
		}
	}
}
