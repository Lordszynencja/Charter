package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Config;
import log.charter.data.config.Localization.Label;
import log.charter.data.managers.RepeatManager;
import log.charter.gui.CharterFrame;
import log.charter.gui.components.SpecialMenuItem;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.panes.StretchPane;

class MusicMenuHandler extends CharterMenuHandler {
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private RepeatManager repeatManager;

	public void init(final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final RepeatManager repeatManager) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.repeatManager = repeatManager;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = new JMenu(Label.MUSIC_MENU.label());
		menu.add(createItem(Label.MUSIC_MENU_25, () -> changeSpeed(25)));
		menu.add(createItem(Label.MUSIC_MENU_50, () -> changeSpeed(50)));
		menu.add(createItem(Label.MUSIC_MENU_75, () -> changeSpeed(75)));
		menu.add(createItem(Label.MUSIC_MENU_CUSTOM, this::customSpeed));

		menu.addSeparator();
		menu.add(new SpecialMenuItem(Label.MUSIC_MENU_TOGGLE_REPEATER, "F6", repeatManager::toggle));
		menu.add(new SpecialMenuItem(Label.MUSIC_MENU_SET_REPEATER_START, "[",
				() -> repeatManager.toggleRepeatStart(data.time)));
		menu.add(new SpecialMenuItem(Label.MUSIC_MENU_SET_REPEATER_END, "]",
				() -> repeatManager.toggleRepeatEnd(data.time)));

		return menu;
	}

	private void changeSpeed(final int speed) {
		Config.stretchedMusicSpeed = speed;
		Config.markChanged();

		audioHandler.clear();
		audioHandler.addSpeedToStretch();
	}

	public void customSpeed() {
		new StretchPane(audioHandler, frame);
	}
}
