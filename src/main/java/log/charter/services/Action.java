package log.charter.services;

import static java.util.Arrays.asList;
import static log.charter.services.editModes.EditMode.GUITAR;
import static log.charter.services.editModes.EditMode.SHOWLIGHTS;
import static log.charter.services.editModes.EditMode.TEMPO_MAP;
import static log.charter.services.editModes.EditMode.VOCALS;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.config.Localization.Label;
import log.charter.services.editModes.EditMode;

public enum Action {
	ADD_GRID_TRIPLET(EditMode.withItems), //
	ARRANGEMENT_NEXT(EditMode.nonEmpty), //
	ARRANGEMENT_PREVIOUS(EditMode.nonEmpty), //
	BEAT_ADD(TEMPO_MAP), //
	BEAT_REMOVE(TEMPO_MAP), //
	BOOKMARK_0(EditMode.nonEmpty), //
	BOOKMARK_1(EditMode.nonEmpty), //
	BOOKMARK_2(EditMode.nonEmpty), //
	BOOKMARK_3(EditMode.nonEmpty), //
	BOOKMARK_4(EditMode.nonEmpty), //
	BOOKMARK_5(EditMode.nonEmpty), //
	BOOKMARK_6(EditMode.nonEmpty), //
	BOOKMARK_7(EditMode.nonEmpty), //
	BOOKMARK_8(EditMode.nonEmpty), //
	BOOKMARK_9(EditMode.nonEmpty), //
	BPM_DOUBLE(TEMPO_MAP), //
	BPM_HALVE(TEMPO_MAP), //
	CHANGE_GRID(EditMode.nonEmpty), //
	CHANGE_TO_POWER_CHORD_BIG(GUITAR), //
	CHANGE_TO_POWER_CHORD_SMALL(GUITAR), //
	COPY(EditMode.nonEmpty), //
	CUT(EditMode.withItems), //
	DECREASE_LENGTH(GUITAR, VOCALS), //
	DECREASE_LENGTH_FAST(GUITAR, VOCALS), //
	DELETE(EditMode.withItems), //
	DELETE_RELATED(GUITAR), //
	DOUBLE_GRID(EditMode.withItems), //
	EDIT_VOCALS(VOCALS), //
	EXIT(EditMode.values()), //
	FAST_BACKWARD(EditMode.nonEmpty), //
	FAST_FORWARD(EditMode.nonEmpty), //
	FINGER_1(GUITAR), //
	FINGER_2(GUITAR), //
	FINGER_3(GUITAR), //
	FINGER_4(GUITAR), //
	FINGER_T(GUITAR), //
	HALVE_GRID(EditMode.withItems), //
	INCREASE_LENGTH(GUITAR, VOCALS), //
	INCREASE_LENGTH_FAST(GUITAR, VOCALS), //
	INSERT_EVENT_POINT(GUITAR), //
	INSERT_FHP(GUITAR), //
	INSERT_HAND_SHAPE(GUITAR), //
	INSERT_SHOWLIGHT(SHOWLIGHTS), //
	INSERT_TONE_CHANGE(GUITAR), //
	INSERT_VOCAL(VOCALS), //
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
	MARK_HAND_SHAPE(GUITAR), //
	MEASURE_ADD(TEMPO_MAP), //
	MEASURE_REMOVE(TEMPO_MAP), //
	MOVE_BACKWARD(EditMode.nonEmpty), //
	MOVE_FORWARD(EditMode.nonEmpty), //
	MOVE_FRET_DOWN(GUITAR), //
	MOVE_FRET_DOWN_OCTAVE(GUITAR), //
	MOVE_FRET_UP(GUITAR), //
	MOVE_FRET_UP_OCTAVE(GUITAR), //
	MOVE_STRING_DOWN(GUITAR), //
	MOVE_STRING_DOWN_SIMPLE(GUITAR), //
	MOVE_STRING_UP(GUITAR), //
	MOVE_STRING_UP_SIMPLE(GUITAR), //
	MOVE_TO_END(EditMode.nonEmpty), //
	MOVE_TO_FIRST_ITEM(EditMode.nonEmpty), //
	MOVE_TO_LAST_ITEM(EditMode.nonEmpty), //
	MOVE_TO_START(EditMode.nonEmpty), //
	NEW_PROJECT(EditMode.values()), //
	NEXT_BEAT(EditMode.nonEmpty), //
	NEXT_GRID_POSITION(EditMode.withItems), //
	NEXT_ITEM(EditMode.nonEmpty), //
	NEXT_ITEM_WITH_SELECT(EditMode.withItems), //
	NEXT_ITEM_WITH_SELECT_CTRL(EditMode.withItems), //
	NEXT_ITEM_WITH_SELECT_CTRL_SHIFT(EditMode.withItems), //
	NEXT_ITEM_WITH_SELECT_SHIFT(EditMode.withItems), //
	NEXT_ITEM_TYPE(GUITAR), //
	OPEN_PROJECT(EditMode.values()), //
	PASTE(EditMode.withItems), //
	PLACE_LYRIC_FROM_TEXT(VOCALS), //
	PLAY_AUDIO(EditMode.nonEmpty), //
	PREVIOUS_BEAT(EditMode.nonEmpty), //
	PREVIOUS_GRID_POSITION(EditMode.withItems), //
	PREVIOUS_ITEM(EditMode.nonEmpty), //
	PREVIOUS_ITEM_WITH_SELECT(EditMode.withItems), //
	PREVIOUS_ITEM_WITH_SELECT_CTRL(EditMode.withItems), //
	PREVIOUS_ITEM_WITH_SELECT_CTRL_SHIFT(EditMode.withItems), //
	PREVIOUS_ITEM_WITH_SELECT_SHIFT(EditMode.withItems), //
	PREVIOUS_ITEM_TYPE(GUITAR), //
	REDO(EditMode.nonEmpty), //
	REMOVE_GRID_TRIPLET(EditMode.withItems), //
	SAVE_PROJECT(EditMode.nonEmpty), //
	SAVE_PROJECT_AS(EditMode.nonEmpty), //
	SELECT_ALL(EditMode.withItems), //
	SELECT_LIKE(GUITAR), //
	SET_HAND_SHAPE_TEMPLATE_ON_CHORDS(GUITAR), //
	SLOW_BACKWARD(EditMode.nonEmpty), //
	SLOW_FORWARD(EditMode.nonEmpty), //
	SNAP_ALL(EditMode.withItems), //
	SNAP_SELECTED(EditMode.withItems), //
	SPECIAL_PASTE(GUITAR), //
	SPEED_DECREASE(EditMode.nonEmpty), //
	SPEED_DECREASE_FAST(EditMode.nonEmpty), //
	SPEED_DECREASE_PRECISE(EditMode.nonEmpty), //
	SPEED_INCREASE(EditMode.nonEmpty), //
	SPEED_INCREASE_FAST(EditMode.nonEmpty), //
	SPEED_INCREASE_PRECISE(EditMode.nonEmpty), //
	STRING_1(TEMPO_MAP, GUITAR), //
	STRING_2(TEMPO_MAP, GUITAR), //
	STRING_3(TEMPO_MAP, GUITAR), //
	STRING_4(TEMPO_MAP, GUITAR), //
	STRING_5(TEMPO_MAP, GUITAR), //
	STRING_6(TEMPO_MAP, GUITAR), //
	STRING_7(TEMPO_MAP, GUITAR), //
	STRING_8(TEMPO_MAP, GUITAR), //
	STRING_9(TEMPO_MAP, GUITAR), //
	SWITCH_TYPING_PART(TEMPO_MAP, GUITAR), //
	TOGGLE_ACCENT(GUITAR), //
	TOGGLE_ACCENT_INDEPENDENTLY(GUITAR), //
	TOGGLE_ANCHOR(TEMPO_MAP), //
	TOGGLE_BAND_PASS_FILTER(EditMode.nonEmpty), //
	TOGGLE_BORDERLESS_PREVIEW_WINDOW(EditMode.nonEmpty), //
	TOGGLE_CLAPS(EditMode.nonEmpty), //
	TOGGLE_HARMONIC(GUITAR), //
	TOGGLE_HARMONIC_INDEPENDENTLY(GUITAR), //
	TOGGLE_HIGH_PASS_FILTER(EditMode.nonEmpty), //
	TOGGLE_HOPO(GUITAR), //
	TOGGLE_HOPO_INDEPENDENTLY(GUITAR), //
	TOGGLE_IGNORE(GUITAR), //
	TOGGLE_IGNORE_INDEPENDENTLY(GUITAR), //
	TOGGLE_LINK_NEXT(GUITAR), //
	TOGGLE_LINK_NEXT_INDEPENDENTLY(GUITAR), //
	TOGGLE_LOW_PASS_FILTER(EditMode.nonEmpty), //
	TOGGLE_METRONOME(EditMode.nonEmpty), //
	TOGGLE_MIDI(EditMode.nonEmpty), //
	TOGGLE_MUTE(GUITAR), //
	TOGGLE_MUTE_INDEPENDENTLY(GUITAR), //
	TOGGLE_NOTE_1(GUITAR), //
	TOGGLE_NOTE_2(GUITAR), //
	TOGGLE_NOTE_3(GUITAR), //
	TOGGLE_NOTE_4(GUITAR), //
	TOGGLE_NOTE_5(GUITAR), //
	TOGGLE_NOTE_6(GUITAR), //
	TOGGLE_NOTE_7(GUITAR), //
	TOGGLE_NOTE_8(GUITAR), //
	TOGGLE_NOTE_9(GUITAR), //
	TOGGLE_ONLY_BOX(GUITAR), //
	TOGGLE_ONLY_BOX_INDEPENDENTLY(GUITAR), //
	TOGGLE_PASS_NOTES(GUITAR), //
	TOGGLE_PASS_NOTES_INDEPENDENTLY(GUITAR), //
	TOGGLE_PHRASE_END(VOCALS), //
	TOGGLE_SLAP_POP(GUITAR), //
	TOGGLE_SLAP_POP_INDEPENDENTLY(GUITAR), //
	TOGGLE_SPLIT(GUITAR), //
	TOGGLE_SPLIT_INDEPENDENTLY(GUITAR), //
	TOGGLE_PREVIEW_WINDOW(EditMode.values()), //
	TOGGLE_REPEAT_END(GUITAR), //
	TOGGLE_REPEAT_START(GUITAR), //
	TOGGLE_REPEATER(GUITAR), //
	TOGGLE_TREMOLO(GUITAR), //
	TOGGLE_TREMOLO_INDEPENDENTLY(GUITAR), //
	TOGGLE_VIBRATO(GUITAR), //
	TOGGLE_VIBRATO_INDEPENDENTLY(GUITAR), //
	TOGGLE_WAVEFORM_GRAPH(EditMode.withItems), //
	TOGGLE_WORD_PART(VOCALS), //
	UNDO(EditMode.nonEmpty), //
	ZOOM_IN(EditMode.nonEmpty), //
	ZOOM_IN_FAST(EditMode.nonEmpty), //
	ZOOM_OUT(EditMode.nonEmpty), //
	ZOOM_OUT_FAST(EditMode.nonEmpty),//

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
