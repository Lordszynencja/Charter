package log.charter.gui.menuHandlers;

import javax.swing.JMenu;

import log.charter.data.ChartData;
import log.charter.data.config.Localization.Label;
import log.charter.gui.CharterFrame;
import log.charter.gui.handlers.AudioHandler;
import log.charter.gui.handlers.mouseAndKeyboard.Action;
import log.charter.gui.handlers.mouseAndKeyboard.KeyboardHandler;
import log.charter.gui.panes.StretchPane;

class MusicMenuHandler extends CharterMenuHandler {
	private AudioHandler audioHandler;
	private ChartData data;
	private CharterFrame frame;
	private KeyboardHandler keyboardHandler;

	public void init(final AudioHandler audioHandler, final ChartData data, final CharterFrame frame,
			final KeyboardHandler keyboardHandler) {
		this.audioHandler = audioHandler;
		this.data = data;
		this.frame = frame;
		this.keyboardHandler = keyboardHandler;
	}

	@Override
	boolean isApplicable() {
		return !data.isEmpty;
	}

	@Override
	JMenu prepareMenu() {
		final JMenu menu = createMenu(Label.MUSIC_MENU);
		menu.add(createItem(keyboardHandler, Action.TOGGLE_REPEATER));
		menu.add(createItem(keyboardHandler, Action.TOGGLE_REPEAT_START));
		menu.add(createItem(keyboardHandler, Action.TOGGLE_REPEAT_END));

		return menu;
	}

	public void customSpeed() {
		new StretchPane(audioHandler, frame);
	}
}
