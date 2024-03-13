package log.charter.gui.menuHandlers;

import javax.swing.JMenu;
import javax.swing.JOptionPane;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songSettings.ArrangementSettingsPane;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.selection.SelectionManager;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

public class ArrangementMenuHandler extends CharterMenuHandler implements Initiable {
	private ActionHandler actionHandler;
	private ChartData chartData;
	private CharterFrame charterFrame;
	private CharterMenuBar charterMenuBar;
	private ModeManager modeManager;
	private SelectionManager selectionManager;

	@Override
	public void init() {
		super.init(actionHandler);
	}

	@Override
	boolean isApplicable() {
		return modeManager.getMode() != EditMode.EMPTY;
	}

	private void addArrangementsList(final JMenu menu) {
		for (int i = 0; i < chartData.songChart.arrangements.size(); i++) {
			final Arrangement arrangement = chartData.songChart.arrangements.get(i);
			final String arrangementName = arrangement.getTypeNameLabel(i);

			final int arrangementId = i;
			menu.add(createItem(arrangementName, () -> modeManager.setArrangement(arrangementId)));
		}
	}

	private void createLevelMenuItems(final JMenu menu) {
		for (int level = 0; level < chartData.currentArrangement().levels.size(); level++) {
			final int levelToChangeTo = level;
			menu.add(createItem("Level " + level, () -> modeManager.setLevel(levelToChangeTo)));
		}
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.ARRANGEMENT_MENU);

		menu.add(createItem(Label.ARRANGEMENT_MENU_TEMPO_MAP, () -> modeManager.setMode(EditMode.TEMPO_MAP)));
		menu.add(createItem(Label.ARRANGEMENT_MENU_VOCALS, () -> modeManager.setMode(EditMode.VOCALS)));
		addArrangementsList(menu);
		menu.add(createItem("New arrangement...", this::addArrangement));

		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.addSeparator();
			menu.add(createItem(Label.ARRANGEMENT_OPTIONS, this::editOptions));

			menu.addSeparator();
			createLevelMenuItems(menu);
		}

		menu.addSeparator();
		menu.add(createItem(Action.TOGGLE_MIDI));
		menu.add(createItem(Action.TOGGLE_CLAPS));
		menu.add(createItem(Action.TOGGLE_METRONOME));
		menu.add(createItem(Action.TOGGLE_WAVEFORM_GRAPH));

		if (modeManager.getMode() == EditMode.GUITAR) {
			menu.addSeparator();
			menu.add(createItem(Label.DELETE_ARRANGEMENT, this::deleteArrangement));
		}

		return menu;
	}

	private void addArrangement() {
		final int previousArrangement = chartData.currentArrangement;
		final EditMode previousEditMode = modeManager.getMode();
		final int previousDifficulty = chartData.currentLevel;
		chartData.currentArrangement = chartData.songChart.arrangements.size();
		chartData.songChart.arrangements.add(new Arrangement(ArrangementType.Lead));
		modeManager.setArrangement(chartData.songChart.arrangements.size() - 1);

		new ArrangementSettingsPane(charterMenuBar, chartData, charterFrame, selectionManager, () -> {
			if (previousEditMode == EditMode.GUITAR) {
				modeManager.setArrangement(previousArrangement);
				modeManager.setLevel(previousDifficulty);
			} else {
				modeManager.setMode(previousEditMode);
			}

			chartData.songChart.arrangements.remove(chartData.songChart.arrangements.size() - 1);

			charterMenuBar.refreshMenus();
		}, true);
	}

	private void editOptions() {
		new ArrangementSettingsPane(charterMenuBar, chartData, charterFrame, selectionManager, null, false);
	}

	private void deleteArrangement() {
		final String arrangementName = chartData.getCurrentArrangementName();
		final String msg = Label.DELETE_ARRANGEMENT_POPUP_MSG.label().formatted(arrangementName);
		final int option = JOptionPane.showConfirmDialog(charterFrame, msg,
				Label.DELETE_ARRANGEMENT_POPUP_TITLE.label(), JOptionPane.YES_NO_OPTION);

		if (option != JOptionPane.YES_OPTION) {
			return;
		}

		final int arrangementToRemove = chartData.currentArrangement;
		chartData.songChart.arrangements.remove(arrangementToRemove);

		if (chartData.songChart.arrangements.size() > 0) {
			if (chartData.songChart.arrangements.size() == arrangementToRemove) {
				modeManager.setArrangement(arrangementToRemove - 1);
			} else {
				modeManager.setArrangement(arrangementToRemove);
			}
		} else {
			modeManager.setMode(EditMode.VOCALS);
		}
	}
}
