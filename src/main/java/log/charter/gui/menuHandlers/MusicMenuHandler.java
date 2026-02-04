package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.components.toolbar.AudioStemsSettings;
import log.charter.gui.components.toolbar.ChartToolbar;
import log.charter.services.Action;
import log.charter.services.data.ProjectAudioHandler;
import log.charter.services.editModes.EditMode;
import log.charter.services.editModes.ModeManager;

class MusicMenuHandler extends CharterMenuHandler {
	private ChartData chartData;
	private ChartToolbar chartToolbar;
	private ModeManager modeManager;
	private ProjectAudioHandler projectAudioHandler;

	@Override
	boolean isApplicable() {
		return modeManager.getMode() != EditMode.EMPTY;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.MUSIC_MENU);

		menu.add(createItem(Action.TOGGLE_MIDI));
		menu.add(createItem(Action.TOGGLE_CLAPS));
		menu.add(createItem(Action.TOGGLE_METRONOME));
		menu.add(createItem(Action.TOGGLE_WAVEFORM_GRAPH));

		menu.addSeparator();
		menu.add(createItem(Action.TOGGLE_REPEATER));
		menu.add(createItem(Action.TOGGLE_REPEAT_START));
		menu.add(createItem(Action.TOGGLE_REPEAT_END));

		menu.addSeparator();
		menu.add(createItem(Label.AUDIO_STEM_SETTINGS, this::openStemSettings));

		return menu;
	}

	private void openStemSettings() {
		new AudioStemsSettings(chartData, charterFrame, chartToolbar, projectAudioHandler);
	}
}
