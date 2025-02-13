package log.charter.services.data.selection;

import log.charter.data.types.PositionType;

public enum SelectionType {
	BEAT, //
	CHORD, //
	FHP, //
	HAND_SHAPE, //
	NOTE, //
	VOCAL;

	public static SelectionType fromPositionType(final PositionType type) {
		switch (type) {
			case BEAT:
				return BEAT;
			case FHP:
				return FHP;
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