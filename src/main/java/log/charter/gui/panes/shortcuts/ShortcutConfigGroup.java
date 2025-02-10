package log.charter.gui.panes.shortcuts;

import static java.util.Arrays.asList;

import java.util.List;

import log.charter.data.config.Localization.Label;
import log.charter.services.Action;

public enum ShortcutConfigGroup {
	TIME_MOVEMENT(Label.TIME_MOVEMENT, //
			Action.PLAY_AUDIO, //
			Action.MOVE_BACKWARD, //
			Action.MOVE_FORWARD, //
			Action.FAST_BACKWARD, //
			Action.FAST_FORWARD, //
			Action.SLOW_BACKWARD, //
			Action.SLOW_FORWARD, //
			Action.MOVE_TO_START, //
			Action.MOVE_TO_END, //
			Action.MOVE_TO_FIRST_ITEM, //
			Action.MOVE_TO_LAST_ITEM, //
			Action.NEXT_ITEM, //
			Action.PREVIOUS_ITEM, //
			Action.NEXT_ITEM_WITH_SELECT, //
			Action.PREVIOUS_ITEM_WITH_SELECT, //
			Action.NEXT_GRID, //
			Action.PREVIOUS_GRID, //
			Action.NEXT_BEAT, //
			Action.PREVIOUS_BEAT, //
			Action.SPEED_DECREASE, //
			Action.SPEED_DECREASE_FAST, //
			Action.SPEED_DECREASE_PRECISE, //
			Action.SPEED_INCREASE, //
			Action.SPEED_INCREASE_FAST, //
			Action.SPEED_INCREASE_PRECISE, //
			Action.TOGGLE_REPEATER, //
			Action.TOGGLE_REPEAT_START, //
			Action.TOGGLE_REPEAT_END), //
	EDITING(Label.EDITING, //
			Action.COPY, //
			Action.PASTE, //
			Action.SPECIAL_PASTE, //
			Action.DELETE, //
			Action.UNDO, //
			Action.REDO, //
			Action.SELECT_ALL_NOTES, //
			Action.REDO, //
			Action.TOGGLE_ANCHOR), //
	VOCAL_EDITING(Label.VOCAL_EDITING, //
			Action.EDIT_VOCALS, //
			Action.TOGGLE_PHRASE_END, //
			Action.TOGGLE_WORD_PART), //
	GUITAR_EDITING(Label.GUITAR_EDITING, //
			Action.MOVE_STRING_UP, //
			Action.MOVE_STRING_DOWN, //
			Action.MOVE_STRING_UP_SIMPLE, //
			Action.MOVE_STRING_DOWN_SIMPLE, //
			Action.MOVE_FRET_UP, //
			Action.MOVE_FRET_DOWN, //
			Action.MARK_HAND_SHAPE, //
			Action.DOUBLE_GRID, //
			Action.HALVE_GRID, //
			Action.SNAP_SELECTED, //
			Action.SNAP_ALL, //
			Action.TOGGLE_ACCENT, //
			Action.TOGGLE_ACCENT_INDEPENDENTLY, //
			Action.TOGGLE_HARMONIC, //
			Action.TOGGLE_HARMONIC_INDEPENDENTLY, //
			Action.TOGGLE_HOPO, //
			Action.TOGGLE_HOPO_INDEPENDENTLY, //
			Action.TOGGLE_LINK_NEXT, //
			Action.TOGGLE_LINK_NEXT_INDEPENDENTLY, //
			Action.TOGGLE_MUTE, //
			Action.TOGGLE_MUTE_INDEPENDENTLY, //
			Action.TOGGLE_TREMOLO, //
			Action.TOGGLE_TREMOLO_INDEPENDENTLY, //
			Action.TOGGLE_VIBRATO, //
			Action.TOGGLE_VIBRATO_INDEPENDENTLY), //
	OTHER(Label.OTHER, //
			Action.NEW_PROJECT, //
			Action.OPEN_PROJECT, //
			Action.SAVE, //
			Action.SAVE_AS, //
			Action.TOGGLE_PREVIEW_WINDOW, //
			Action.TOGGLE_BORDERLESS_PREVIEW_WINDOW, //
			Action.TOGGLE_MIDI, //
			Action.TOGGLE_CLAPS, //
			Action.TOGGLE_METRONOME, //
			Action.TOGGLE_WAVEFORM_GRAPH, //
			Action.EXIT);

	public static final List<ShortcutConfigGroup> groups = asList(//
			TIME_MOVEMENT, //
			EDITING, //
			VOCAL_EDITING, //
			GUITAR_EDITING, //
			OTHER);

	public final Label label;
	public final List<Action> actions;

	private ShortcutConfigGroup(final Label label, final Action... actions) {
		this.label = label;
		this.actions = asList(actions);
	}
}
