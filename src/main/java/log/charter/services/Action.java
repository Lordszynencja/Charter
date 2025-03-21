package log.charter.services;

import static java.util.Arrays.asList;
import static log.charter.services.editModes.EditMode.EMPTY;
import static log.charter.services.editModes.EditMode.GUITAR;
import static log.charter.services.editModes.EditMode.TEMPO_MAP;
import static log.charter.services.editModes.EditMode.VOCALS;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.config.Localization.Label;
import log.charter.services.editModes.EditMode;

public enum Action {
	ARRANGEMENT_NEXT(TEMPO_MAP, VOCALS, GUITAR), //
	ARRANGEMENT_PREVIOUS(TEMPO_MAP, VOCALS, GUITAR), //
	BEAT_ADD(TEMPO_MAP), //
	BEAT_REMOVE(TEMPO_MAP), //
	BPM_DOUBLE(TEMPO_MAP), //
	BPM_HALVE(TEMPO_MAP), //
	COPY(TEMPO_MAP, VOCALS, GUITAR), //
	DELETE(VOCALS, GUITAR), //
	DOUBLE_GRID(VOCALS, GUITAR), //
	EDIT_VOCALS(VOCALS), //
	EXIT(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	FAST_BACKWARD(TEMPO_MAP, VOCALS, GUITAR), //
	FAST_FORWARD(TEMPO_MAP, VOCALS, GUITAR), //
	NUMBER_0(TEMPO_MAP, GUITAR), //
	NUMBER_1(TEMPO_MAP, GUITAR), //
	NUMBER_2(TEMPO_MAP, GUITAR), //
	NUMBER_3(TEMPO_MAP, GUITAR), //
	NUMBER_4(TEMPO_MAP, GUITAR), //
	NUMBER_5(TEMPO_MAP, GUITAR), //
	NUMBER_6(TEMPO_MAP, GUITAR), //
	NUMBER_7(TEMPO_MAP, GUITAR), //
	NUMBER_8(TEMPO_MAP, GUITAR), //
	NUMBER_9(TEMPO_MAP, GUITAR), //
	HALVE_GRID(VOCALS, GUITAR), //
	MARK_BOOKMARK_0(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_1(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_2(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_3(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_4(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_5(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_6(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_7(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_8(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_BOOKMARK_9(TEMPO_MAP, VOCALS, GUITAR), //
	MARK_HAND_SHAPE(GUITAR), //
	MEASURE_ADD(TEMPO_MAP), //
	MEASURE_REMOVE(TEMPO_MAP), //
	MOVE_BACKWARD(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_FORWARD(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_FRET_DOWN(GUITAR), //
	MOVE_FRET_DOWN_OCTAVE(GUITAR), //
	MOVE_FRET_UP(GUITAR), //
	MOVE_FRET_UP_OCTAVE(GUITAR), //
	MOVE_STRING_DOWN(GUITAR), //
	MOVE_STRING_DOWN_SIMPLE(GUITAR), //
	MOVE_STRING_UP(GUITAR), //
	MOVE_STRING_UP_SIMPLE(GUITAR), //
	MOVE_TO_BOOKMARK_0(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_1(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_2(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_3(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_4(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_5(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_6(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_7(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_8(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_BOOKMARK_9(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_END(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_FIRST_ITEM(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_LAST_ITEM(TEMPO_MAP, VOCALS, GUITAR), //
	MOVE_TO_START(TEMPO_MAP, VOCALS, GUITAR), //
	NEW_PROJECT(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	NEXT_BEAT(VOCALS, GUITAR), //
	NEXT_GRID(Label.NEXT_GRID_POSITION, VOCALS, GUITAR), //
	NEXT_ITEM(TEMPO_MAP, VOCALS, GUITAR), //
	NEXT_ITEM_WITH_SELECT(VOCALS, GUITAR), //
	OPEN_PROJECT(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	PASTE(VOCALS, GUITAR), //
	PLACE_LYRIC_FROM_TEXT(VOCALS), //
	PLAY_AUDIO(TEMPO_MAP, VOCALS, GUITAR), //
	PREVIOUS_BEAT(VOCALS, GUITAR), //
	PREVIOUS_GRID(Label.PREVIOUS_GRID_POSITION, VOCALS, GUITAR), //
	PREVIOUS_ITEM(TEMPO_MAP, VOCALS, GUITAR), //
	PREVIOUS_ITEM_WITH_SELECT(VOCALS, GUITAR), //
	REDO(TEMPO_MAP, VOCALS, GUITAR), //
	SAVE(Label.SAVE_PROJECT, TEMPO_MAP, VOCALS, GUITAR), //
	SAVE_AS(Label.SAVE_PROJECT_AS, TEMPO_MAP, VOCALS, GUITAR), //
	SELECT_ALL(VOCALS, GUITAR), //
	SLOW_BACKWARD(TEMPO_MAP, VOCALS, GUITAR), //
	SLOW_FORWARD(TEMPO_MAP, VOCALS, GUITAR), //
	SNAP_ALL(VOCALS, GUITAR), //
	SNAP_SELECTED(VOCALS, GUITAR), //
	SPECIAL_PASTE(GUITAR), //
	SPEED_DECREASE(TEMPO_MAP, VOCALS, GUITAR), //
	SPEED_DECREASE_FAST(TEMPO_MAP, VOCALS, GUITAR), //
	SPEED_DECREASE_PRECISE(TEMPO_MAP, VOCALS, GUITAR), //
	SPEED_INCREASE(TEMPO_MAP, VOCALS, GUITAR), //
	SPEED_INCREASE_FAST(TEMPO_MAP, VOCALS, GUITAR), //
	SPEED_INCREASE_PRECISE(TEMPO_MAP, VOCALS, GUITAR), //
	SWITCH_TS_TYPING_PART(TEMPO_MAP), //
	TOGGLE_ACCENT(GUITAR), //
	TOGGLE_ACCENT_INDEPENDENTLY(GUITAR), //
	TOGGLE_ANCHOR(TEMPO_MAP), //
	TOGGLE_BAND_PASS_FILTER(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_BORDERLESS_PREVIEW_WINDOW(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_CLAPS(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_HARMONIC(GUITAR), //
	TOGGLE_HARMONIC_INDEPENDENTLY(GUITAR), //
	TOGGLE_HIGH_PASS_FILTER(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_HOPO(GUITAR), //
	TOGGLE_HOPO_INDEPENDENTLY(GUITAR), //
	TOGGLE_LINK_NEXT(GUITAR), //
	TOGGLE_LINK_NEXT_INDEPENDENTLY(GUITAR), //
	TOGGLE_LOW_PASS_FILTER(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_METRONOME(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_MIDI(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_MUTE(GUITAR), //
	TOGGLE_MUTE_INDEPENDENTLY(GUITAR), //
	TOGGLE_PHRASE_END(VOCALS), //
	TOGGLE_PREVIEW_WINDOW(EMPTY, TEMPO_MAP, VOCALS, GUITAR), //
	TOGGLE_REPEAT_END(GUITAR), //
	TOGGLE_REPEAT_START(GUITAR), //
	TOGGLE_REPEATER(GUITAR), //
	TOGGLE_TREMOLO(GUITAR), //
	TOGGLE_TREMOLO_INDEPENDENTLY(GUITAR), //
	TOGGLE_VIBRATO(GUITAR), //
	TOGGLE_VIBRATO_INDEPENDENTLY(GUITAR), //
	TOGGLE_WAVEFORM_GRAPH(VOCALS, GUITAR), //
	TOGGLE_WORD_PART(VOCALS), //
	UNDO(TEMPO_MAP, VOCALS, GUITAR),//

	;

	public final Label label;
	public final Set<EditMode> editModes;

	private Action(final Label label, final EditMode... editModes) {
		this.label = label;
		this.editModes = new HashSet<>(asList(editModes));
	}

	private Action(final EditMode... editModes) {
		label = Label.valueOf(name());
		this.editModes = new HashSet<>(asList(editModes));
	}

}
