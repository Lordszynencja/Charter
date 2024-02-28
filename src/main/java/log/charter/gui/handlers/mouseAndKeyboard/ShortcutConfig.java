package log.charter.gui.handlers.mouseAndKeyboard;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.managers.modes.EditMode;
import log.charter.io.Logger;
import log.charter.util.RW;

public class ShortcutConfig {
	private static final String shortcutConfigPath = new File(RW.getProgramDirectory(), "shortcuts.ini")
			.getAbsolutePath();

	private static Map<Action, Shortcut> shortcuts = new HashMap<>();
	private static Map<EditMode, Map<Shortcut, Action>> actions = new HashMap<>();
	static {
		for (final EditMode editMode : EditMode.values()) {
			actions.put(editMode, new HashMap<>());
		}
	}

	private static boolean changed = false;

	public static void setShortcut(final Action action, final Shortcut shortcut) {
		shortcuts.put(action, shortcut);
		for (final EditMode editMode : action.editModes) {
			actions.get(editMode).put(shortcut, action);
		}
	}

	public static void init() {
		final Map<String, String> config = RW.readConfig(shortcutConfigPath);
		for (final Entry<String, String> entry : config.entrySet()) {
			try {
				final Action action = Action.valueOf(entry.getKey());
				final Shortcut shortcut = Shortcut.fromName(entry.getValue());
				setShortcut(action, shortcut);
			} catch (final Exception e) {
				Logger.error(shortcutConfigPath);
			}
		}

		for (final Action action : Action.values()) {
			if (shortcuts.containsKey(action)) {
				continue;
			}

			setShortcut(action, action.defaultShortcut);
		}

		setChanged();
		save();
	}

	public static void save() {
		if (!changed) {
			return;
		}

		final Map<String, String> config = new HashMap<>();
		shortcuts.forEach((action, shortcut) -> config.put(action.name(), shortcut.saveName()));
		RW.writeConfig(shortcutConfigPath, config);

		changed = false;
	}

	public static Action getAction(final EditMode editMode, final Shortcut shortcut) {
		return actions.get(editMode).get(shortcut);
	}

	public static Shortcut getShortcut(final Action action) {
		return shortcuts.get(action);
	}

	public static void setChanged() {
		changed = true;
	}
}
