package log.charter.gui.handlers.mouseAndKeyboard;

import java.awt.event.KeyEvent;

import log.charter.data.config.Localization.Label;

public enum Action {
	DELETE(new Shortcut(KeyEvent.VK_DELETE), Label.DELETE), //
	FAST_LEFT(new Shortcut(KeyEvent.VK_LEFT).shift(), null), //
	FAST_RIGHT(new Shortcut(KeyEvent.VK_RIGHT).shift(), null), //
	LEFT(new Shortcut(KeyEvent.VK_LEFT), null), //
	NEXT_BEAT(new Shortcut(KeyEvent.VK_RIGHT).shift().alt(), null), //
	NEXT_GRID(new Shortcut(KeyEvent.VK_RIGHT).ctrl().alt(), null), //
	NEXT_SOUND(new Shortcut(KeyEvent.VK_RIGHT).alt(), null), //
	PLAY_AUDIO(new Shortcut(KeyEvent.VK_SPACE), null), //
	PREVIOUS_BEAT(new Shortcut(KeyEvent.VK_LEFT).shift().alt(), null), //
	PREVIOUS_GRID(new Shortcut(KeyEvent.VK_LEFT).ctrl().alt(), null), //
	PREVIOUS_SOUND(new Shortcut(KeyEvent.VK_LEFT).alt(), null), //
	RIGHT(new Shortcut(KeyEvent.VK_RIGHT), null), //
	SLOW_LEFT(new Shortcut(KeyEvent.VK_LEFT).ctrl(), null), //
	SLOW_RIGHT(new Shortcut(KeyEvent.VK_RIGHT).ctrl(), null)

	;

	public final Shortcut defaultShortcut;
	public final Label label;

	private Action(final Shortcut defaultShortcut, final Label label) {
		this.defaultShortcut = defaultShortcut;
		this.label = label;
	}

}
