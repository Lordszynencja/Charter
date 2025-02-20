package log.charter.services.mouseAndKeyboard.shortcuts;

import java.awt.event.KeyEvent;

import log.charter.services.Action;
import log.charter.services.mouseAndKeyboard.Shortcut;

public class DefaultEofShortcuts extends ShortcutList {
	public static final ShortcutList instance = new DefaultEofShortcuts();

	private DefaultEofShortcuts() {
		super.set(Action.ARRANGEMENT_NEXT, new Shortcut(KeyEvent.VK_UP).ctrl().shift());
		super.set(Action.ARRANGEMENT_PREVIOUS, new Shortcut(KeyEvent.VK_DOWN).ctrl().shift());
		super.set(Action.COPY, new Shortcut(KeyEvent.VK_C).ctrl());
		super.set(Action.DELETE, new Shortcut(KeyEvent.VK_DELETE));
		super.set(Action.DOUBLE_GRID, new Shortcut(KeyEvent.VK_PERIOD));
		super.set(Action.EDIT_VOCALS, new Shortcut(KeyEvent.VK_L));
		super.set(Action.EXIT, new Shortcut(KeyEvent.VK_ESCAPE));
		super.set(Action.FAST_BACKWARD, new Shortcut(KeyEvent.VK_LEFT).shift());
		super.set(Action.FAST_FORWARD, new Shortcut(KeyEvent.VK_RIGHT).shift());
		super.set(Action.NUMBER_0, new Shortcut(KeyEvent.VK_0));
		super.set(Action.NUMBER_1, new Shortcut(KeyEvent.VK_1));
		super.set(Action.NUMBER_2, new Shortcut(KeyEvent.VK_2));
		super.set(Action.NUMBER_3, new Shortcut(KeyEvent.VK_3));
		super.set(Action.NUMBER_4, new Shortcut(KeyEvent.VK_4));
		super.set(Action.NUMBER_5, new Shortcut(KeyEvent.VK_5));
		super.set(Action.NUMBER_6, new Shortcut(KeyEvent.VK_6));
		super.set(Action.NUMBER_7, new Shortcut(KeyEvent.VK_7));
		super.set(Action.NUMBER_8, new Shortcut(KeyEvent.VK_8));
		super.set(Action.NUMBER_9, new Shortcut(KeyEvent.VK_9));
		super.set(Action.HALVE_GRID, new Shortcut(KeyEvent.VK_COMMA));
		super.set(Action.MARK_BOOKMARK_0, new Shortcut(KeyEvent.VK_0).ctrl());
		super.set(Action.MARK_BOOKMARK_1, new Shortcut(KeyEvent.VK_1).ctrl());
		super.set(Action.MARK_BOOKMARK_2, new Shortcut(KeyEvent.VK_2).ctrl());
		super.set(Action.MARK_BOOKMARK_3, new Shortcut(KeyEvent.VK_3).ctrl());
		super.set(Action.MARK_BOOKMARK_4, new Shortcut(KeyEvent.VK_4).ctrl());
		super.set(Action.MARK_BOOKMARK_5, new Shortcut(KeyEvent.VK_5).ctrl());
		super.set(Action.MARK_BOOKMARK_6, new Shortcut(KeyEvent.VK_6).ctrl());
		super.set(Action.MARK_BOOKMARK_7, new Shortcut(KeyEvent.VK_7).ctrl());
		super.set(Action.MARK_BOOKMARK_8, new Shortcut(KeyEvent.VK_8).ctrl());
		super.set(Action.MARK_BOOKMARK_9, new Shortcut(KeyEvent.VK_9).ctrl());
		super.set(Action.MARK_HAND_SHAPE, new Shortcut(KeyEvent.VK_H).ctrl());
		super.set(Action.MOVE_BACKWARD, new Shortcut(KeyEvent.VK_LEFT));
		super.set(Action.MOVE_FORWARD, new Shortcut(KeyEvent.VK_RIGHT));
		super.set(Action.MOVE_FRET_DOWN, new Shortcut(KeyEvent.VK_DOWN).shift());
		super.set(Action.MOVE_FRET_UP, new Shortcut(KeyEvent.VK_UP).shift());
		super.set(Action.MOVE_STRING_DOWN, new Shortcut(KeyEvent.VK_DOWN).alt());
		super.set(Action.MOVE_STRING_DOWN_SIMPLE, new Shortcut(KeyEvent.VK_DOWN));
		super.set(Action.MOVE_STRING_UP, new Shortcut(KeyEvent.VK_UP).alt());
		super.set(Action.MOVE_STRING_UP_SIMPLE, new Shortcut(KeyEvent.VK_UP));
		super.set(Action.MOVE_TO_BOOKMARK_0, new Shortcut(KeyEvent.VK_0).shift());
		super.set(Action.MOVE_TO_BOOKMARK_1, new Shortcut(KeyEvent.VK_1).shift());
		super.set(Action.MOVE_TO_BOOKMARK_2, new Shortcut(KeyEvent.VK_2).shift());
		super.set(Action.MOVE_TO_BOOKMARK_3, new Shortcut(KeyEvent.VK_3).shift());
		super.set(Action.MOVE_TO_BOOKMARK_4, new Shortcut(KeyEvent.VK_4).shift());
		super.set(Action.MOVE_TO_BOOKMARK_5, new Shortcut(KeyEvent.VK_5).shift());
		super.set(Action.MOVE_TO_BOOKMARK_6, new Shortcut(KeyEvent.VK_6).shift());
		super.set(Action.MOVE_TO_BOOKMARK_7, new Shortcut(KeyEvent.VK_7).shift());
		super.set(Action.MOVE_TO_BOOKMARK_8, new Shortcut(KeyEvent.VK_8).shift());
		super.set(Action.MOVE_TO_BOOKMARK_9, new Shortcut(KeyEvent.VK_9).shift());
		super.set(Action.MOVE_TO_END, new Shortcut(KeyEvent.VK_END));
		super.set(Action.MOVE_TO_FIRST_ITEM, new Shortcut(KeyEvent.VK_HOME).ctrl());
		super.set(Action.MOVE_TO_LAST_ITEM, new Shortcut(KeyEvent.VK_END).ctrl());
		super.set(Action.MOVE_TO_START, new Shortcut(KeyEvent.VK_HOME));
		super.set(Action.NEW_PROJECT, new Shortcut(KeyEvent.VK_N).ctrl());
		super.set(Action.NEXT_BEAT, new Shortcut(KeyEvent.VK_PAGE_UP));
		super.set(Action.NEXT_GRID, new Shortcut(KeyEvent.VK_PAGE_UP).ctrl().shift());
		super.set(Action.NEXT_ITEM, new Shortcut(KeyEvent.VK_PAGE_UP).shift());
		super.set(Action.NEXT_ITEM_WITH_SELECT, new Shortcut(KeyEvent.VK_PAGE_UP).alt());
		super.set(Action.OPEN_PROJECT, new Shortcut(KeyEvent.VK_O).ctrl());
		super.set(Action.PASTE, new Shortcut(KeyEvent.VK_V).ctrl());
		super.set(Action.PLACE_LYRIC_FROM_TEXT, new Shortcut(KeyEvent.VK_SPACE).ctrl());
		super.set(Action.PLAY_AUDIO, new Shortcut(KeyEvent.VK_SPACE));
		super.set(Action.PREVIOUS_BEAT, new Shortcut(KeyEvent.VK_PAGE_DOWN));
		super.set(Action.PREVIOUS_GRID, new Shortcut(KeyEvent.VK_PAGE_DOWN).ctrl().shift());
		super.set(Action.PREVIOUS_ITEM, new Shortcut(KeyEvent.VK_PAGE_DOWN).shift());
		super.set(Action.PREVIOUS_ITEM_WITH_SELECT, new Shortcut(KeyEvent.VK_PAGE_DOWN).alt());
		super.set(Action.REDO, new Shortcut(KeyEvent.VK_R).ctrl());
		super.set(Action.SAVE, new Shortcut(KeyEvent.VK_S).ctrl());
		super.set(Action.SAVE_AS, new Shortcut(KeyEvent.VK_S).ctrl().shift());
		super.set(Action.SELECT_ALL, new Shortcut(KeyEvent.VK_A).ctrl());
		super.set(Action.SLOW_BACKWARD, new Shortcut(KeyEvent.VK_LEFT).ctrl());
		super.set(Action.SLOW_FORWARD, new Shortcut(KeyEvent.VK_RIGHT).ctrl());
		super.set(Action.SNAP_ALL, new Shortcut(KeyEvent.VK_R).ctrl().shift());
		super.set(Action.SNAP_SELECTED, new Shortcut(KeyEvent.VK_R).alt());
		super.set(Action.SPECIAL_PASTE, new Shortcut(KeyEvent.VK_V).ctrl().shift());
		super.set(Action.SPEED_DECREASE, new Shortcut(KeyEvent.VK_MINUS));
		super.set(Action.SPEED_DECREASE_FAST, new Shortcut(KeyEvent.VK_MINUS).shift());
		super.set(Action.SPEED_DECREASE_PRECISE, new Shortcut(KeyEvent.VK_MINUS).ctrl());
		super.set(Action.SPEED_INCREASE, new Shortcut(KeyEvent.VK_PLUS));
		super.set(Action.SPEED_INCREASE_FAST, new Shortcut(KeyEvent.VK_PLUS).shift());
		super.set(Action.SPEED_INCREASE_PRECISE, new Shortcut(KeyEvent.VK_PLUS).ctrl());
		super.set(Action.SWITCH_TS_TYPING_PART, new Shortcut(KeyEvent.VK_SLASH));
		super.set(Action.TOGGLE_ACCENT, new Shortcut(KeyEvent.VK_A));
		super.set(Action.TOGGLE_ACCENT_INDEPENDENTLY, new Shortcut(KeyEvent.VK_A).ctrl().shift());
		super.set(Action.TOGGLE_ANCHOR, new Shortcut(KeyEvent.VK_A).shift());
		super.set(Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW, new Shortcut(KeyEvent.VK_F12));
		super.set(Action.TOGGLE_CLAPS, new Shortcut(KeyEvent.VK_C));
		super.set(Action.TOGGLE_HARMONIC, null);
		super.set(Action.TOGGLE_HARMONIC_INDEPENDENTLY, new Shortcut(KeyEvent.VK_H).shift());
		super.set(Action.TOGGLE_HOPO, null);
		super.set(Action.TOGGLE_HOPO_INDEPENDENTLY, new Shortcut(KeyEvent.VK_H));
		super.set(Action.TOGGLE_LINK_NEXT, null);
		super.set(Action.TOGGLE_LINK_NEXT_INDEPENDENTLY, new Shortcut(KeyEvent.VK_N).shift());
		super.set(Action.TOGGLE_METRONOME, new Shortcut(KeyEvent.VK_M));
		super.set(Action.TOGGLE_MIDI, new Shortcut(KeyEvent.VK_F2));
		super.set(Action.TOGGLE_MUTE, null);
		super.set(Action.TOGGLE_MUTE_INDEPENDENTLY, new Shortcut(KeyEvent.VK_M).shift());
		super.set(Action.TOGGLE_PHRASE_END, new Shortcut(KeyEvent.VK_E));
		super.set(Action.TOGGLE_PREVIEW_WINDOW, new Shortcut(KeyEvent.VK_F11));
		super.set(Action.TOGGLE_REPEAT_END, new Shortcut(KeyEvent.VK_CLOSE_BRACKET).alt());
		super.set(Action.TOGGLE_REPEAT_START, new Shortcut(KeyEvent.VK_OPEN_BRACKET).alt());
		super.set(Action.TOGGLE_REPEATER, null);
		super.set(Action.TOGGLE_TREMOLO, null);
		super.set(Action.TOGGLE_TREMOLO_INDEPENDENTLY, new Shortcut(KeyEvent.VK_O).ctrl().shift());
		super.set(Action.TOGGLE_VIBRATO, null);
		super.set(Action.TOGGLE_VIBRATO_INDEPENDENTLY, new Shortcut(KeyEvent.VK_V).shift());
		super.set(Action.TOGGLE_WAVEFORM_GRAPH, new Shortcut(KeyEvent.VK_F5));
		super.set(Action.TOGGLE_WORD_PART, new Shortcut(KeyEvent.VK_W));
		super.set(Action.UNDO, new Shortcut(KeyEvent.VK_Z).ctrl());
	}

	@Override
	public void set(final Action action, final Shortcut shortcut) {
		throw new UnsupportedOperationException("Can't change default values");
	}

}
