package log.charter.services;

import static java.util.Arrays.asList;
import static log.charter.services.editModes.EditMode.GUITAR;
import static log.charter.services.editModes.EditMode.TEMPO_MAP;
import static log.charter.services.editModes.EditMode.VOCALS;

import java.util.HashSet;
import java.util.Set;

import log.charter.data.config.Localization.Label;
import log.charter.services.editModes.EditMode;

public enum Action {
	ARRANGEMENT_NEXT(EditMode.nonEmpty), //
	ARRANGEMENT_PREVIOUS(EditMode.nonEmpty), //
	BEAT_ADD(TEMPO_MAP), //
	BEAT_REMOVE(TEMPO_MAP), //
	BPM_DOUBLE(TEMPO_MAP), //
	BPM_HALVE(TEMPO_MAP), //
	COPY(EditMode.nonEmpty), //
	DELETE(EditMode.withItems), //
	DELETE_RELATED(GUITAR), //
	DOUBLE_GRID(EditMode.withItems), //
	EDIT_VOCALS(VOCALS), //
	EXIT(EditMode.values()), //
	FAST_BACKWARD(EditMode.nonEmpty), //
	FAST_FORWARD(EditMode.nonEmpty), //
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
	HALVE_GRID(EditMode.withItems), //
	MARK_BOOKMARK_0(EditMode.nonEmpty), //
	MARK_BOOKMARK_1(EditMode.nonEmpty), //
	MARK_BOOKMARK_2(EditMode.nonEmpty), //
	MARK_BOOKMARK_3(EditMode.nonEmpty), //
	MARK_BOOKMARK_4(EditMode.nonEmpty), //
	MARK_BOOKMARK_5(EditMode.nonEmpty), //
	MARK_BOOKMARK_6(EditMode.nonEmpty), //
	MARK_BOOKMARK_7(EditMode.nonEmpty), //
	MARK_BOOKMARK_8(EditMode.nonEmpty), //
	MARK_BOOKMARK_9(EditMode.nonEmpty), //
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
	MOVE_TO_BOOKMARK_0(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_1(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_2(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_3(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_4(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_5(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_6(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_7(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_8(EditMode.nonEmpty), //
	MOVE_TO_BOOKMARK_9(EditMode.nonEmpty), //
	MOVE_TO_END(EditMode.nonEmpty), //
	MOVE_TO_FIRST_ITEM(EditMode.nonEmpty), //
	MOVE_TO_LAST_ITEM(EditMode.nonEmpty), //
	MOVE_TO_START(EditMode.nonEmpty), //
	NEW_PROJECT(EditMode.nonEmpty), //
	NEXT_BEAT(EditMode.nonEmpty), //
	NEXT_GRID(Label.NEXT_GRID_POSITION, EditMode.withItems), //
	NEXT_ITEM(EditMode.nonEmpty), //
	NEXT_ITEM_WITH_SELECT(VOCALS, GUITAR), //
	OPEN_PROJECT(EditMode.nonEmpty), //
	PASTE(VOCALS, GUITAR), //
	PLACE_LYRIC_FROM_TEXT(VOCALS), //
	PLAY_AUDIO(EditMode.nonEmpty), //
	PREVIOUS_BEAT(EditMode.nonEmpty), //
	PREVIOUS_GRID(Label.PREVIOUS_GRID_POSITION, EditMode.withItems), //
	PREVIOUS_ITEM(EditMode.nonEmpty), //
	PREVIOUS_ITEM_WITH_SELECT(VOCALS, GUITAR), //
	REDO(EditMode.nonEmpty), //
	SAVE(Label.SAVE_PROJECT, EditMode.nonEmpty), //
	SAVE_AS(Label.SAVE_PROJECT_AS, EditMode.nonEmpty), //
	SELECT_ALL(EditMode.withItems), //
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
	SWITCH_TS_TYPING_PART(TEMPO_MAP), //
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
	TOGGLE_LINK_NEXT(GUITAR), //
	TOGGLE_LINK_NEXT_INDEPENDENTLY(GUITAR), //
	TOGGLE_LOW_PASS_FILTER(EditMode.nonEmpty), //
	TOGGLE_METRONOME(EditMode.nonEmpty), //
	TOGGLE_MIDI(EditMode.nonEmpty), //
	TOGGLE_MUTE(GUITAR), //
	TOGGLE_MUTE_INDEPENDENTLY(GUITAR), //
	TOGGLE_PHRASE_END(VOCALS), //
	TOGGLE_PREVIEW_WINDOW(EditMode.values()), //
	TOGGLE_REPEAT_END(GUITAR), //
	TOGGLE_REPEAT_START(GUITAR), //
	TOGGLE_REPEATER(GUITAR), //
	TOGGLE_TREMOLO(GUITAR), //
	TOGGLE_TREMOLO_INDEPENDENTLY(GUITAR), //
	TOGGLE_VIBRATO(GUITAR), //
	TOGGLE_VIBRATO_INDEPENDENTLY(GUITAR), //
	TOGGLE_WAVEFORM_GRAPH(VOCALS, GUITAR), //
	TOGGLE_WORD_PART(VOCALS), //
	UNDO(EditMode.nonEmpty),//

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
