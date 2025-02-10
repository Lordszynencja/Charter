package log.charter.services.mouseAndKeyboard.shortcuts;

import java.util.HashMap;
import java.util.Map;

import log.charter.services.Action;
import log.charter.services.mouseAndKeyboard.Shortcut;

public class ShortcutList {
	private final Map<Action, Shortcut> shortcuts = new HashMap<>();

	public void set(final Action action, final Shortcut shortcut) {
		shortcuts.put(action, shortcut);
	}

	public Shortcut get(final Action action) {
		final Shortcut shortcut = shortcuts.get(action);
		return shortcut == null ? null : new Shortcut(shortcut);
	}
}
