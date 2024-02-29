package log.charter.gui.handlers.mouseAndKeyboard;

import static log.charter.data.managers.modes.EditMode.EMPTY;
import static log.charter.data.managers.modes.EditMode.GUITAR;
import static log.charter.data.managers.modes.EditMode.TEMPO_MAP;
import static log.charter.data.managers.modes.EditMode.VOCALS;

import java.awt.event.KeyEvent;

import log.charter.data.config.Localization.Label;
import log.charter.data.managers.modes.EditMode;

public enum Action {
	COPY(new Shortcut(KeyEvent.VK_C).ctrl(), Label.COPY, TEMPO_MAP, VOCALS, GUITAR), //
	DELETE(new Shortcut(KeyEvent.VK_DELETE), Label.DELETE, TEMPO_MAP, VOCALS, GUITAR), //
	DOUBLE_GRID(new Shortcut(KeyEvent.VK_PERIOD), Label.DOUBLE_GRID, VOCALS, GUITAR), //
	EDIT_VOCALS(new Shortcut(KeyEvent.VK_L), Label.EDIT_VOCALS, VOCALS), //
	EXIT(new Shortcut(KeyEvent.VK_ESCAPE), Label.EXIT, EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	FAST_BACKWARD(new Shortcut(KeyEvent.VK_LEFT).shift(), Label.FAST_BACKWARD, TEMPO_MAP, VOCALS, GUITAR), //
	FAST_FORWARD(new Shortcut(KeyEvent.VK_RIGHT).shift(), Label.FAST_FORWARD, TEMPO_MAP, VOCALS, GUITAR), //
	HALVE_GRID(new Shortcut(KeyEvent.VK_COMMA), Label.HALVE_GRID, VOCALS, GUITAR), //
	MARK_HAND_SHAPE(new Shortcut(KeyEvent.VK_H).ctrl(), Label.MARK_HAND_SHAPE, GUITAR), //
	MOVE_BACKWARD(new Shortcut(KeyEvent.VK_LEFT), Label.MOVE_BACKWARD, TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_FORWARD(new Shortcut(KeyEvent.VK_RIGHT), Label.MOVE_FORWARD, TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_FRET_DOWN(new Shortcut(KeyEvent.VK_DOWN).alt(), Label.MOVE_FRET_DOWN, GUITAR), //
	MOVE_FRET_UP(new Shortcut(KeyEvent.VK_UP).alt(), Label.MOVE_FRET_UP, GUITAR), //
	MOVE_STRING_DOWN(new Shortcut(KeyEvent.VK_DOWN), Label.MOVE_STRING_DOWN, GUITAR), //
	MOVE_STRING_DOWN_SIMPLE(new Shortcut(KeyEvent.VK_DOWN).ctrl(), Label.MOVE_STRING_DOWN_SIMPLE, GUITAR), //
	MOVE_STRING_UP(new Shortcut(KeyEvent.VK_UP), Label.MOVE_STRING_UP, GUITAR), //
	MOVE_STRING_UP_SIMPLE(new Shortcut(KeyEvent.VK_UP).ctrl(), Label.MOVE_STRING_UP_SIMPLE, GUITAR), //
	MOVE_TO_END(new Shortcut(KeyEvent.VK_END), Label.MOVE_TO_END, TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_FIRST_ITEM(new Shortcut(KeyEvent.VK_HOME).ctrl(), Label.MOVE_TO_FIRST_ITEM, TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_LAST_ITEM(new Shortcut(KeyEvent.VK_END).ctrl(), Label.MOVE_TO_LAST_ITEM, TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_START(new Shortcut(KeyEvent.VK_HOME), Label.MOVE_TO_START, TEMPO_MAP, VOCALS, GUITAR), //
	NEW_PROJECT(new Shortcut(KeyEvent.VK_N).ctrl(), Label.NEW_PROJECT, EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	NEXT_BEAT(new Shortcut(KeyEvent.VK_RIGHT).shift().alt(), Label.NEXT_BEAT, VOCALS, GUITAR), //
	NEXT_GRID(new Shortcut(KeyEvent.VK_RIGHT).ctrl().alt(), Label.NEXT_GRID_POSITION, VOCALS, GUITAR), //
	NEXT_ITEM(new Shortcut(KeyEvent.VK_RIGHT).alt(), Label.NEXT_ITEM, TEMPO_MAP, VOCALS, GUITAR), //
	OPEN_PROJECT(new Shortcut(KeyEvent.VK_O).ctrl(), Label.OPEN_PROJECT, EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	PASTE(new Shortcut(KeyEvent.VK_V).ctrl(), Label.PASTE, VOCALS, GUITAR), //
	PLAY_AUDIO(new Shortcut(KeyEvent.VK_SPACE), Label.PLAY_AUDIO, TEMPO_MAP, VOCALS, GUITAR), //
	PREVIOUS_BEAT(new Shortcut(KeyEvent.VK_LEFT).shift().alt(), Label.PREVIOUS_BEAT, VOCALS, GUITAR), //
	PREVIOUS_GRID(new Shortcut(KeyEvent.VK_LEFT).ctrl().alt(), Label.PREVIOUS_GRID_POSITION, VOCALS, GUITAR), //
	PREVIOUS_ITEM(new Shortcut(KeyEvent.VK_LEFT).alt(), Label.PREVIOUS_ITEM, TEMPO_MAP, VOCALS, GUITAR), //
	REDO(new Shortcut(KeyEvent.VK_R).ctrl(), Label.REDO, TEMPO_MAP, VOCALS, GUITAR), //
	SAVE(new Shortcut(KeyEvent.VK_S).ctrl(), Label.SAVE_PROJECT, TEMPO_MAP, VOCALS, GUITAR), //
	SAVE_AS(new Shortcut(KeyEvent.VK_S).ctrl().shift(), Label.SAVE_PROJECT_AS, TEMPO_MAP, VOCALS, GUITAR), //
	SELECT_ALL_NOTES(new Shortcut(KeyEvent.VK_A).ctrl(), Label.SELECT_ALL_NOTES), //
	SLOW_BACKWARD(new Shortcut(KeyEvent.VK_LEFT).ctrl(), Label.SLOW_BACKWARD, TEMPO_MAP, VOCALS, GUITAR), //
	SLOW_FORWARD(new Shortcut(KeyEvent.VK_RIGHT).ctrl(), Label.SLOW_FORWARD, TEMPO_MAP, VOCALS, GUITAR), //
	SNAP_ALL(new Shortcut(KeyEvent.VK_G).ctrl().shift(), Label.SNAP_ALL, VOCALS, GUITAR), //
	SNAP_SELECTED(new Shortcut(KeyEvent.VK_G).ctrl(), Label.SNAP_SELECTED, VOCALS, GUITAR), //
	SPECIAL_PASTE(new Shortcut(KeyEvent.VK_V).ctrl().shift(), Label.SPECIAL_PASTE, GUITAR), //
	TOGGLE_ACCENT(new Shortcut(KeyEvent.VK_A), Label.TOGGLE_ACCENT, GUITAR), //
	TOGGLE_ACCENT_INDEPENDENTLY(new Shortcut(KeyEvent.VK_A).alt(), Label.TOGGLE_ACCENT_INDEPENDENTLY, GUITAR), //
	TOGGLE_BORDERLESS_PREVIEW_WINDOW(new Shortcut(KeyEvent.VK_F12), Label.TOGGLE_BORDERLESS_PREVIEW_WINDOW, EMPTY,
			TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_CLAPS(new Shortcut(KeyEvent.VK_F3), Label.TOGGLE_CLAPS, EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_HARMONIC(new Shortcut(KeyEvent.VK_O), Label.TOGGLE_HARMONIC, GUITAR), //
	TOGGLE_HARMONIC_INDEPENDENTLY(new Shortcut(KeyEvent.VK_O).alt(), Label.TOGGLE_HARMONIC_INDEPENDENTLY, GUITAR), //
	TOGGLE_HOPO(new Shortcut(KeyEvent.VK_H), Label.TOGGLE_HOPO, GUITAR), //
	TOGGLE_HOPO_INDEPENDENTLY(new Shortcut(KeyEvent.VK_H).alt(), Label.TOGGLE_HOPO_INDEPENDENTLY, GUITAR), //
	TOGGLE_LINK_NEXT(new Shortcut(KeyEvent.VK_L), Label.TOGGLE_LINK_NEXT, GUITAR), //
	TOGGLE_LINK_NEXT_INDEPENDENTLY(new Shortcut(KeyEvent.VK_L).alt(), Label.TOGGLE_LINK_NEXT_INDEPENDENTLY, GUITAR), //
	TOGGLE_METRONOME(new Shortcut(KeyEvent.VK_F4), Label.TOGGLE_METRONOME, EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_MIDI(new Shortcut(KeyEvent.VK_F2), Label.TOGGLE_MIDI, EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_MUTE(new Shortcut(KeyEvent.VK_M), Label.TOGGLE_MUTE, GUITAR), //
	TOGGLE_MUTE_INDEPENDENTLY(new Shortcut(KeyEvent.VK_M).alt(), Label.TOGGLE_MUTE_INDEPENDENTLY, GUITAR), //
	TOGGLE_PHRASE_END(new Shortcut(KeyEvent.VK_E), Label.TOGGLE_PHRASE_END, VOCALS), //
	TOGGLE_PREVIEW_WINDOW(new Shortcut(KeyEvent.VK_F11), Label.TOGGLE_PREVIEW_WINDOW, EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_REPEAT_END(new Shortcut(KeyEvent.VK_CLOSE_BRACKET), Label.TOGGLE_REPEAT_START, GUITAR), //
	TOGGLE_REPEAT_START(new Shortcut(KeyEvent.VK_OPEN_BRACKET), Label.TOGGLE_REPEAT_END, GUITAR), //
	TOGGLE_REPEATER(new Shortcut(KeyEvent.VK_F6), Label.TOGGLE_REPEATER, GUITAR), //
	TOGGLE_TREMOLO(new Shortcut(KeyEvent.VK_T), Label.TOGGLE_TREMOLO, GUITAR), //
	TOGGLE_TREMOLO_INDEPENDENTLY(new Shortcut(KeyEvent.VK_T).alt(), Label.TOGGLE_TREMOLO_INDEPENDENTLY, GUITAR), //
	TOGGLE_VIBRATO(new Shortcut(KeyEvent.VK_V), Label.TOGGLE_VIBRATO, GUITAR), //
	TOGGLE_VIBRATO_INDEPENDENTLY(new Shortcut(KeyEvent.VK_V).alt(), Label.TOGGLE_VIBRATO_INDEPENDENTLY, GUITAR), //
	TOGGLE_WAVEFORM_GRAPH(new Shortcut(KeyEvent.VK_F5), Label.TOGGLE_WAVEFORM_GRAPH, VOCALS, GUITAR), //
	TOGGLE_WORD_PART(new Shortcut(KeyEvent.VK_W), Label.TOGGLE_WORD_PART, VOCALS), //
	UNDO(new Shortcut(KeyEvent.VK_Z).ctrl(), Label.UNDO, TEMPO_MAP, VOCALS, GUITAR),//

	;

	public final Shortcut defaultShortcut;
	public final Label label;
	public final EditMode[] editModes;

	private Action(final Shortcut defaultShortcut, final Label label, final EditMode... editModes) {
		this.defaultShortcut = defaultShortcut;
		this.label = label;
		this.editModes = editModes;
	}

}
