package log.charter.song.enums;

import log.charter.io.rs.xml.song.ArrangementNote;

public enum HOPO {
	HAMMER_ON, PULL_OFF, TAP, NONE;

	public static HOPO fromArrangmentNote(final ArrangementNote note) {
		if (note.hammerOn != null && note.hammerOn == 1) {
			return HAMMER_ON;
		}
		if (note.pullOff != null && note.pullOff == 1) {
			return PULL_OFF;
		}
		if (note.tap != null && note.tap == 1) {
			return TAP;
		}

		return NONE;
	}
}
