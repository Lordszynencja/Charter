package log.charter.gui.menuHandlers;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.song.Arrangement;
import log.charter.data.song.vocals.VocalPath;
import log.charter.gui.CharterFrame;
import log.charter.gui.panes.songSettings.ArrangementSettingsPane;
import log.charter.gui.panes.songSettings.VocalPathSettingsPane;
import log.charter.io.rs.xml.song.ArrangementType;
import log.charter.services.Action;
import log.charter.services.ActionHandler;
import log.charter.services.CharterContext.Initiable;
import log.charter.services.data.LevelSquisher;
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

	private String getNameWithSelect(final String name, final boolean isSelected) {
		if (isSelected) {
			return "> " + name;
		}

		return name;
	}

	private String getNameWithSelect(final Label label, final boolean isSelected) {
		return getNameWithSelect(label.label(), isSelected);
	}

	private void addVocalPathsList(final JMenu menu) {
		for (int i = 0; i < chartData.songChart.vocalPaths.size(); i++) {
			final VocalPath vocalPath = chartData.songChart.vocalPaths.get(i);
			final boolean vocalsSelected = i == chartData.currentVocals && modeManager.getMode() == EditMode.VOCALS;
			final int vocalPathId = i;

			final String vocalsLabel = getNameWithSelect(vocalPath.getName(vocalPathId), vocalsSelected);

			final JMenuItem menuItem = createItem(vocalsLabel, () -> modeManager.setVocalPath(vocalPathId));
			if (vocalPath.color != null) {
				menuItem.setForeground(vocalPath.color);
			}
			menuItem.setFont(menuItem.getFont().deriveFont(Font.BOLD));
			menu.add(menuItem);
		}
	}

	private void addArrangementsList(final JMenu menu) {
		for (int i = 0; i < chartData.songChart.arrangements.size(); i++) {
			final Arrangement arrangement = chartData.songChart.arrangements.get(i);
			final boolean arrangementSelected = i == chartData.currentArrangement
					&& modeManager.getMode() == EditMode.GUITAR;
			final String arrangementLabel = getNameWithSelect(arrangement.getTypeNameLabel(i), arrangementSelected);
			final int arrangementId = i;

			final JMenuItem menuItem = createItem(arrangementLabel, () -> modeManager.setArrangement(arrangementId));
			menuItem.setForeground(switch (arrangement.arrangementType) {
				case Lead -> new Color(255, 200, 80);
				case Rhythm -> new Color(120, 255, 120);
				case Bass -> new Color(80, 120, 255);
				default -> new Color(255, 255, 120);
			});
			menuItem.setFont(menuItem.getFont().deriveFont(Font.BOLD));
			menu.add(menuItem);
		}
	}

	private void createLevelMenuItems(final JMenu menu) {
		for (int level = 0; level < chartData.currentArrangement().levels.size(); level++) {
			final boolean isLevelSelected = level == chartData.currentLevel && modeManager.getMode() == EditMode.GUITAR;
			final String levelLabel = getNameWithSelect("Level " + level, isLevelSelected);
			final int levelToChangeTo = level;
			menu.add(createItem(levelLabel, () -> modeManager.setLevel(levelToChangeTo)));
		}
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.ARRANGEMENT_MENU);

		final String tempoMapLabel = getNameWithSelect(Label.ARRANGEMENT_MENU_TEMPO_MAP,
				modeManager.getMode() == EditMode.TEMPO_MAP);
		menu.add(createItem(tempoMapLabel, () -> modeManager.setMode(EditMode.TEMPO_MAP)));

		menu.addSeparator();
		addVocalPathsList(menu);
		menu.add(createItem(Label.NEW_VOCAL_PATH, this::addVocalPath));

		menu.addSeparator();
		addArrangementsList(menu);
		menu.add(createItem(Label.NEW_ARRANGEMENT, this::addArrangement));

		menu.addSeparator();
		menu.add(createItem(Action.ARRANGEMENT_NEXT));
		menu.add(createItem(Action.ARRANGEMENT_PREVIOUS));

		if (modeManager.getMode() == EditMode.VOCALS) {
			menu.addSeparator();
			menu.add(createItem(Label.VOCAL_PATH_OPTIONS, this::editVocalPathSettings));

			menu.addSeparator();
			menu.add(createItem(Label.DELETE_VOCAL_PATH, this::deleteVocalPath));
		} else if (modeManager.getMode() == EditMode.GUITAR) {
			menu.addSeparator();
			menu.add(createItem(Label.ARRANGEMENT_OPTIONS, this::editArrangementSettings));

			menu.addSeparator();
			createLevelMenuItems(menu);

			menu.addSeparator();
			menu.add(createItem(Label.SQUASH_LEVELS, this::squashLevels));

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

	private void addVocalPath() {
		new VocalPathSettingsPane(chartData, charterMenuBar, charterFrame, selectionManager, new VocalPath(), true);
	}

	private void editVocalPathSettings() {
		new VocalPathSettingsPane(chartData, charterMenuBar, charterFrame, selectionManager, chartData.currentVocals(),
				false);
	}

	private void editArrangementSettings() {
		new ArrangementSettingsPane(charterMenuBar, chartData, charterFrame, selectionManager, null, false);
	}

	private void squashLevels() {
		modeManager.setLevel(0);
		LevelSquisher.squish(chartData.currentArrangement());
	}

	private void deleteVocalPath() {
		final String name = chartData.getCurrentVocalPathName();
		final String msg = Label.DELETE_VOCAL_PATH_POPUP_MSG.format(name);
		final int option = JOptionPane.showConfirmDialog(charterFrame, msg, Label.DELETE_VOCAL_PATH_POPUP_TITLE.label(),
				JOptionPane.YES_NO_OPTION);

		if (option != JOptionPane.YES_OPTION) {
			return;
		}

		chartData.songChart.vocalPaths.remove(chartData.currentVocals);

		if (!chartData.songChart.vocalPaths.isEmpty()) {
			if (chartData.songChart.vocalPaths.size() == chartData.currentVocals) {
				modeManager.setVocalPath(chartData.currentVocals - 1);
			} else {
				modeManager.resetVocalPath();
			}
		} else {
			modeManager.setMode(EditMode.TEMPO_MAP);
		}
	}

	private void deleteArrangement() {
		final String arrangementName = chartData.getCurrentArrangementName();
		final String msg = Label.DELETE_ARRANGEMENT_POPUP_MSG.format(arrangementName);
		final int option = JOptionPane.showConfirmDialog(charterFrame, msg,
				Label.DELETE_ARRANGEMENT_POPUP_TITLE.label(), JOptionPane.YES_NO_OPTION);

		if (option != JOptionPane.YES_OPTION) {
			return;
		}

		chartData.songChart.arrangements.remove(chartData.currentArrangement);

		if (!chartData.songChart.arrangements.isEmpty()) {
			if (chartData.songChart.arrangements.size() == chartData.currentArrangement) {
				modeManager.setArrangement(chartData.currentArrangement - 1);
			} else {
				modeManager.resetArrangement();
			}
		} else if (!chartData.songChart.vocalPaths.isEmpty()) {
			modeManager.setVocalPath(chartData.songChart.vocalPaths.size() - 1);
		} else {
			modeManager.setMode(EditMode.TEMPO_MAP);
		}
	}
}
