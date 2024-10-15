package log.charter.services.mouseAndKeyboard;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import log.charter.io.Logger;
import log.charter.services.Action;
import log.charter.services.editModes.EditMode;
import log.charter.util.RW;

public class ShortcutConfig {
	private static final String shortcutConfigPath = new File(RW.getProgramDirectory(), "shortcuts.ini")
			.getAbsolutePath();

	private static Map<Action, Shortcut> defaultShortcuts = new HashMap<>();

	private static void addDefaultShortcut(final Action action, final Shortcut shortcut, final EditMode... editModes) {
		defaultShortcuts.put(action, shortcut);
	}

	static {
		addDefaultShortcut(Action.COPY, new Shortcut(KeyEvent.VK_C).ctrl());
		addDefaultShortcut(Action.DELETE, new Shortcut(KeyEvent.VK_DELETE));
		addDefaultShortcut(Action.DOUBLE_GRID, new Shortcut(KeyEvent.VK_PERIOD));
		addDefaultShortcut(Action.EDIT_VOCALS, new Shortcut(KeyEvent.VK_L));
		addDefaultShortcut(Action.EXIT, new Shortcut(KeyEvent.VK_ESCAPE));
		addDefaultShortcut(Action.FAST_BACKWARD, new Shortcut(KeyEvent.VK_LEFT).shift());
		addDefaultShortcut(Action.FAST_FORWARD, new Shortcut(KeyEvent.VK_RIGHT).shift());
		addDefaultShortcut(Action.FRET_0, new Shortcut(KeyEvent.VK_0));
		addDefaultShortcut(Action.FRET_1, new Shortcut(KeyEvent.VK_1));
		addDefaultShortcut(Action.FRET_2, new Shortcut(KeyEvent.VK_2));
		addDefaultShortcut(Action.FRET_3, new Shortcut(KeyEvent.VK_3));
		addDefaultShortcut(Action.FRET_4, new Shortcut(KeyEvent.VK_4));
		addDefaultShortcut(Action.FRET_5, new Shortcut(KeyEvent.VK_5));
		addDefaultShortcut(Action.FRET_6, new Shortcut(KeyEvent.VK_6));
		addDefaultShortcut(Action.FRET_7, new Shortcut(KeyEvent.VK_7));
		addDefaultShortcut(Action.FRET_8, new Shortcut(KeyEvent.VK_8));
		addDefaultShortcut(Action.FRET_9, new Shortcut(KeyEvent.VK_9));
		addDefaultShortcut(Action.HALVE_GRID, new Shortcut(KeyEvent.VK_COMMA));
		addDefaultShortcut(Action.MARK_BOOKMARK_0, new Shortcut(KeyEvent.VK_0).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_1, new Shortcut(KeyEvent.VK_1).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_2, new Shortcut(KeyEvent.VK_2).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_3, new Shortcut(KeyEvent.VK_3).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_4, new Shortcut(KeyEvent.VK_4).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_5, new Shortcut(KeyEvent.VK_5).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_6, new Shortcut(KeyEvent.VK_6).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_7, new Shortcut(KeyEvent.VK_7).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_8, new Shortcut(KeyEvent.VK_8).ctrl());
		addDefaultShortcut(Action.MARK_BOOKMARK_9, new Shortcut(KeyEvent.VK_9).ctrl());
		addDefaultShortcut(Action.MARK_HAND_SHAPE, new Shortcut(KeyEvent.VK_H).ctrl());
		addDefaultShortcut(Action.MOVE_BACKWARD, new Shortcut(KeyEvent.VK_LEFT));
		addDefaultShortcut(Action.MOVE_FORWARD, new Shortcut(KeyEvent.VK_RIGHT));
		addDefaultShortcut(Action.MOVE_FRET_DOWN, new Shortcut(KeyEvent.VK_DOWN).alt());
		addDefaultShortcut(Action.MOVE_FRET_UP, new Shortcut(KeyEvent.VK_UP).alt());
		addDefaultShortcut(Action.MOVE_STRING_DOWN, new Shortcut(KeyEvent.VK_DOWN));
		addDefaultShortcut(Action.MOVE_STRING_DOWN_SIMPLE, new Shortcut(KeyEvent.VK_DOWN).ctrl());
		addDefaultShortcut(Action.MOVE_STRING_UP, new Shortcut(KeyEvent.VK_UP));
		addDefaultShortcut(Action.MOVE_STRING_UP_SIMPLE, new Shortcut(KeyEvent.VK_UP).ctrl());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_0, new Shortcut(KeyEvent.VK_0).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_1, new Shortcut(KeyEvent.VK_1).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_2, new Shortcut(KeyEvent.VK_2).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_3, new Shortcut(KeyEvent.VK_3).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_4, new Shortcut(KeyEvent.VK_4).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_5, new Shortcut(KeyEvent.VK_5).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_6, new Shortcut(KeyEvent.VK_6).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_7, new Shortcut(KeyEvent.VK_7).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_8, new Shortcut(KeyEvent.VK_8).shift());
		addDefaultShortcut(Action.MOVE_TO_BOOKMARK_9, new Shortcut(KeyEvent.VK_9).shift());
		addDefaultShortcut(Action.MOVE_TO_END, new Shortcut(KeyEvent.VK_END));
		addDefaultShortcut(Action.MOVE_TO_FIRST_ITEM, new Shortcut(KeyEvent.VK_HOME).ctrl());
		addDefaultShortcut(Action.MOVE_TO_LAST_ITEM, new Shortcut(KeyEvent.VK_END).ctrl());
		addDefaultShortcut(Action.MOVE_TO_START, new Shortcut(KeyEvent.VK_HOME));
		addDefaultShortcut(Action.NEW_PROJECT, new Shortcut(KeyEvent.VK_N).ctrl());
		addDefaultShortcut(Action.NEXT_BEAT, new Shortcut(KeyEvent.VK_RIGHT).shift().alt());
		addDefaultShortcut(Action.NEXT_GRID, new Shortcut(KeyEvent.VK_RIGHT).ctrl().alt());
		addDefaultShortcut(Action.NEXT_ITEM, new Shortcut(KeyEvent.VK_RIGHT).alt());
		addDefaultShortcut(Action.NEXT_ITEM_WITH_SELECT, new Shortcut(KeyEvent.VK_PAGE_UP));
		addDefaultShortcut(Action.OPEN_PROJECT, new Shortcut(KeyEvent.VK_O).ctrl());
		addDefaultShortcut(Action.PASTE, new Shortcut(KeyEvent.VK_V).ctrl());
		addDefaultShortcut(Action.PLAY_AUDIO, new Shortcut(KeyEvent.VK_SPACE));
		addDefaultShortcut(Action.PREVIOUS_BEAT, new Shortcut(KeyEvent.VK_LEFT).shift().alt());
		addDefaultShortcut(Action.PREVIOUS_GRID, new Shortcut(KeyEvent.VK_LEFT).ctrl().alt());
		addDefaultShortcut(Action.PREVIOUS_ITEM, new Shortcut(KeyEvent.VK_LEFT).alt());
		addDefaultShortcut(Action.PREVIOUS_ITEM_WITH_SELECT, new Shortcut(KeyEvent.VK_PAGE_DOWN));
		addDefaultShortcut(Action.REDO, new Shortcut(KeyEvent.VK_R).ctrl());
		addDefaultShortcut(Action.SAVE, new Shortcut(KeyEvent.VK_S).ctrl());
		addDefaultShortcut(Action.SAVE_AS, new Shortcut(KeyEvent.VK_S).ctrl().shift());
		addDefaultShortcut(Action.SELECT_ALL_NOTES, new Shortcut(KeyEvent.VK_A).ctrl());
		addDefaultShortcut(Action.SLOW_BACKWARD, new Shortcut(KeyEvent.VK_LEFT).ctrl());
		addDefaultShortcut(Action.SLOW_FORWARD, new Shortcut(KeyEvent.VK_RIGHT).ctrl());
		addDefaultShortcut(Action.SNAP_ALL, new Shortcut(KeyEvent.VK_G).ctrl().shift());
		addDefaultShortcut(Action.SNAP_SELECTED, new Shortcut(KeyEvent.VK_G).ctrl());
		addDefaultShortcut(Action.SPECIAL_PASTE, new Shortcut(KeyEvent.VK_V).ctrl().shift());
		addDefaultShortcut(Action.SPEED_DECREASE, new Shortcut(KeyEvent.VK_MINUS));
		addDefaultShortcut(Action.SPEED_DECREASE_FAST, new Shortcut(KeyEvent.VK_MINUS).shift());
		addDefaultShortcut(Action.SPEED_DECREASE_PRECISE, new Shortcut(KeyEvent.VK_MINUS).ctrl());
		addDefaultShortcut(Action.SPEED_INCREASE, new Shortcut(KeyEvent.VK_PLUS));
		addDefaultShortcut(Action.SPEED_INCREASE_FAST, new Shortcut(KeyEvent.VK_PLUS).shift());
		addDefaultShortcut(Action.SPEED_INCREASE_PRECISE, new Shortcut(KeyEvent.VK_PLUS).ctrl());
		addDefaultShortcut(Action.TOGGLE_ACCENT, new Shortcut(KeyEvent.VK_A));
		addDefaultShortcut(Action.TOGGLE_ACCENT_INDEPENDENTLY, new Shortcut(KeyEvent.VK_A).alt());
		addDefaultShortcut(Action.TOGGLE_ANCHOR, new Shortcut(KeyEvent.VK_A));
		addDefaultShortcut(Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW, new Shortcut(KeyEvent.VK_F12));
		addDefaultShortcut(Action.TOGGLE_CLAPS, new Shortcut(KeyEvent.VK_F3));
		addDefaultShortcut(Action.TOGGLE_HARMONIC, new Shortcut(KeyEvent.VK_O));
		addDefaultShortcut(Action.TOGGLE_HARMONIC_INDEPENDENTLY, new Shortcut(KeyEvent.VK_O).alt());
		addDefaultShortcut(Action.TOGGLE_HOPO, new Shortcut(KeyEvent.VK_H));
		addDefaultShortcut(Action.TOGGLE_HOPO_INDEPENDENTLY, new Shortcut(KeyEvent.VK_H).alt());
		addDefaultShortcut(Action.TOGGLE_LINK_NEXT, new Shortcut(KeyEvent.VK_L));
		addDefaultShortcut(Action.TOGGLE_LINK_NEXT_INDEPENDENTLY, new Shortcut(KeyEvent.VK_L).alt());
		addDefaultShortcut(Action.TOGGLE_METRONOME, new Shortcut(KeyEvent.VK_F4));
		addDefaultShortcut(Action.TOGGLE_MIDI, new Shortcut(KeyEvent.VK_F2));
		addDefaultShortcut(Action.TOGGLE_MUTE, new Shortcut(KeyEvent.VK_M));
		addDefaultShortcut(Action.TOGGLE_MUTE_INDEPENDENTLY, new Shortcut(KeyEvent.VK_M).alt());
		addDefaultShortcut(Action.TOGGLE_PHRASE_END, new Shortcut(KeyEvent.VK_E));
		addDefaultShortcut(Action.TOGGLE_PREVIEW_WINDOW, new Shortcut(KeyEvent.VK_F11));
		addDefaultShortcut(Action.TOGGLE_REPEAT_END, new Shortcut(KeyEvent.VK_CLOSE_BRACKET));
		addDefaultShortcut(Action.TOGGLE_REPEAT_START, new Shortcut(KeyEvent.VK_OPEN_BRACKET));
		addDefaultShortcut(Action.TOGGLE_REPEATER, new Shortcut(KeyEvent.VK_F6));
		addDefaultShortcut(Action.TOGGLE_TREMOLO, new Shortcut(KeyEvent.VK_T));
		addDefaultShortcut(Action.TOGGLE_TREMOLO_INDEPENDENTLY, new Shortcut(KeyEvent.VK_T).alt());
		addDefaultShortcut(Action.TOGGLE_VIBRATO, new Shortcut(KeyEvent.VK_V));
		addDefaultShortcut(Action.TOGGLE_VIBRATO_INDEPENDENTLY, new Shortcut(KeyEvent.VK_V).alt());
		addDefaultShortcut(Action.TOGGLE_WAVEFORM_GRAPH, new Shortcut(KeyEvent.VK_F5));
		addDefaultShortcut(Action.TOGGLE_WORD_PART, new Shortcut(KeyEvent.VK_W));
		addDefaultShortcut(Action.UNDO, new Shortcut(KeyEvent.VK_Z).ctrl());
	}

	private static Map<Action, Shortcut> shortcuts = new HashMap<>();
	private static Map<EditMode, Map<Shortcut, Action>> actions = new HashMap<>();
	static {
		for (final EditMode editMode : EditMode.values()) {
			actions.put(editMode, new HashMap<>());
		}
	}

	private static boolean changed = false;

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

		shortcuts.put(action, shortcut);
		for (final EditMode editMode : action.editModes) {
			final Map<Shortcut, Action> editModeActions = actions.get(editMode);
			if (editModeActions.containsKey(shortcut)) {
				Logger.error("doubled shortcut %s: %s and %s".formatted(shortcut.name("-"),
						editModeActions.get(shortcut).label.label(), action.label.label()));
			}

			editModeActions.put(shortcut, action);
		}

		addNumberShortcutAlias(action, shortcut);
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

			setShortcut(action, defaultShortcuts.get(action));
		}

		markChanged();
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

	public static void markChanged() {
		changed = true;
	}
}
