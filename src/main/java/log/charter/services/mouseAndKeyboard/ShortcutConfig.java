package log.charter.services.mouseAndKeyboard;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.data.config.Config;
import log.charter.io.Logger;
import log.charter.services.Action;
import log.charter.services.editModes.EditMode;
import log.charter.services.mouseAndKeyboard.shortcuts.DefaultEofShortcuts;
import log.charter.services.mouseAndKeyboard.shortcuts.DefaultShortcuts;
import log.charter.services.mouseAndKeyboard.shortcuts.ShortcutList;
import log.charter.util.RW;

public class ShortcutConfig {
	private static final String shortcutConfigPath = new File(RW.getProgramDirectory(), "shortcuts.ini")
			.getAbsolutePath();

	public static ShortcutList defaultShortcuts;

	public static ShortcutList shortcuts = new ShortcutList();
	private static Map<EditMode, Map<Shortcut, Action>> actions = new HashMap<>();

	private static boolean changed = false;

	static {
		resetDefaultShortcuts();
		resetEditModeActions();
	}

	public static void resetDefaultShortcuts() {
		defaultShortcuts = Config.defaultEofShortcuts ? DefaultEofShortcuts.instance : DefaultShortcuts.instance;
	}

	private static void addNumberShortcutAlias(final Action action, final Shortcut shortcut) {
		final Shortcut alias = switch (shortcut.key) {
			case KeyEvent.VK_0 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD0);
			case KeyEvent.VK_1 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD1);
			case KeyEvent.VK_2 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD2);
			case KeyEvent.VK_3 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD3);
			case KeyEvent.VK_4 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD4);
			case KeyEvent.VK_5 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD5);
			case KeyEvent.VK_6 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD6);
			case KeyEvent.VK_7 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD7);
			case KeyEvent.VK_8 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD8);
			case KeyEvent.VK_9 -> new Shortcut(shortcut).key(KeyEvent.VK_NUMPAD9);
			default -> null;
		};
		if (alias == null) {
			return;
		}

		for (final EditMode editMode : action.editModes) {
			actions.get(editMode).put(alias, action);
		}
	}

	public static void setShortcut(final Action action, final Shortcut shortcut) {
		if (shortcut == null) {
			return;
		}

		shortcuts.set(action, shortcut);
		for (final EditMode editMode : action.editModes) {
			final Map<Shortcut, Action> editModeActions = actions.get(editMode);
			editModeActions.put(shortcut, action);
		}
		addNumberShortcutAlias(action, shortcut);
	}

	public static void resetEditModeActions() {
		for (final EditMode editMode : EditMode.values()) {
			actions.put(editMode, new HashMap<>());
		}

		for (final Action action : Action.values()) {
			final Shortcut shortcut = shortcuts.get(action);
			if (shortcut == null) {
				continue;
			}

			for (final EditMode editMode : action.editModes) {
				actions.get(editMode).put(shortcut, action);
			}

			addNumberShortcutAlias(action, shortcut);
		}
	}

	public static void init() {
		resetDefaultShortcuts();

		final Map<String, String> config = RW.readConfig(shortcutConfigPath, false);
		for (final Entry<String, String> entry : config.entrySet()) {
			try {
				final Action action = Action.valueOf(entry.getKey());
				final Shortcut shortcut = Shortcut.fromName(entry.getValue());
				for (final EditMode editMode : action.editModes) {
					final Map<Shortcut, Action> editModeActions = actions.get(editMode);
					if (editModeActions.containsKey(shortcut)) {
						Logger.error("Doubled shortcut %s: %s and %s".formatted(shortcut.name("-"),
								editModeActions.get(shortcut).label.label(), action.label.label()));
					}
				}

				setShortcut(action, shortcut);
			} catch (final Exception e) {
				Logger.error(shortcutConfigPath);
			}
		}

		for (final Action action : Action.values()) {
			if (shortcuts.get(action) != null) {
				continue;
			}

			shortcuts.set(action, defaultShortcuts.get(action));
		}

		markChanged();
		save();
	}

	public static void save() {
		if (!changed) {
			return;
		}

		final Map<String, String> config = new HashMap<>();
		for (final Action action : Action.values()) {
			final Shortcut shortcut = shortcuts.get(action);
			if (shortcut != null) {
				config.put(action.name(), shortcut.saveName());
			}
		}

		RW.writeConfig(shortcutConfigPath, config);

		changed = false;
	}

	public static Action getAction(final EditMode editMode, final Shortcut shortcut) {
		return actions.get(editMode).get(shortcut);
	}

	public static void markChanged() {
		changed = true;
	}
}
