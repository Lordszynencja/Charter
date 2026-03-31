package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.data.undoSystem.UndoSystem;
import log.charter.gui.components.simple.SpecialMenuItem;
import log.charter.gui.panes.songEdits.AddBeatsAtTheStartPane;
import log.charter.gui.panes.songEdits.AddSilenceAtTheEndPane;
import log.charter.gui.panes.songEdits.AddSilenceInTheBeginningPane;
import log.charter.gui.panes.songEdits.SetDefaultSilencePane;
import log.charter.services.Action;
import log.charter.services.data.ChartTimeHandler;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class TempoMapMenuHandler extends CharterMenuHandler {
	private ChartData chartData;
	private ChartTimeHandler chartTimeHandler;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;
	private UndoSystem undoSystem;

	@Override
	boolean isApplicable() {
		return modeManager.getMode() == EditMode.TEMPO_MAP;
	}

	private void addAdvancedAudioTempoMapEdits(final JMenu menu) {
		final JMenu submenu = createMenu(Label.ADVANCED_AUDIO_TEMPO_MAP_EDITS);
		submenu.add(new SpecialMenuItem(Label.ADD_SILENCE_IN_THE_BEGINNING, this::addSilenceInTheBeginning));
		submenu.add(new SpecialMenuItem(Label.ADD_BEATS_AT_THE_START, this::addBeatsAtTheStart));
		submenu.add(new SpecialMenuItem(Label.ADD_SILENCE_AT_THE_END, this::addSilenceIAtTheEnd));
		menu.add(submenu);
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.TEMPO_MAP_MENU);

		menu.add(new SpecialMenuItem(Label.SET_DEFAULT_START_SILENCE, this::setDefaultSilence));

		menu.addSeparator();
		addAdvancedAudioTempoMapEdits(menu);

		menu.addSeparator();
		menu.add(createDisabledItem(Action.TOGGLE_ANCHOR));
		menu.add(createDisabledItem(Action.BEAT_ADD));
		menu.add(createDisabledItem(Action.BEAT_REMOVE));
		menu.add(createDisabledItem(Action.BPM_DOUBLE));
		menu.add(createDisabledItem(Action.BPM_HALVE));

		return menu;
	}

	private void setDefaultSilence() {
		new SetDefaultSilencePane(charterFrame, chartTimeHandler, chartData, projectAudioHandler);
	}

	private void addSilenceInTheBeginning() {
		new AddSilenceInTheBeginningPane(charterFrame, chartTimeHandler, chartData, projectAudioHandler);
	}

	private void addBeatsAtTheStart() {
		new AddBeatsAtTheStartPane(charterFrame, chartData, undoSystem);
	}

	private void addSilenceIAtTheEnd() {
		new AddSilenceAtTheEndPane(charterFrame, projectAudioHandler);
	}
}
