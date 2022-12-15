package log.charter.data.managers.selection;

import log.charter.data.types.PositionType;

public enum SelectionType {
	ANCHOR, //
	BEAT, //
	CHORD, //
	HAND_SHAPE, //
	NOTE, //
	VOCAL;

	public static SelectionType fromPositionType(final PositionType type) {
		switch (type) {
		case ANCHOR:
			return ANCHOR;
		case BEAT:
			return BEAT;
		case GUITAR_NOTE:
			return null;
		case HAND_SHAPE:
			return HAND_SHAPE;
		case NONE:
			return null;
		case VOCAL:
			return VOCAL;
		default:
			return null;
		}
	}
}