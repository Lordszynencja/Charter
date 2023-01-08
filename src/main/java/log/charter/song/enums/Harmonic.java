package log.charter.song.enums;

import log.charter.io.rs.xml.song.ArrangementNote;

public enum Harmonic {
	NORMAL, PINCH, NONE;

	public static Harmonic fromArrangmentNote(final ArrangementNote note) {
		if (note.harmonic != null && note.harmonic == 1) {
			return NORMAL;
		}
		if (note.harmonicPinch != null && note.harmonicPinch == 1) {
			return PINCH;
		}

		return NONE;
	}
}
