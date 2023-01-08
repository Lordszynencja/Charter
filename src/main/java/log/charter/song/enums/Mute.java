package log.charter.song.enums;

import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementNote;

public enum Mute {
	STRING, PALM, NONE;

	public static Mute fromArrangmentNote(final ArrangementNote note) {
		if (note.mute != null && note.mute == 1) {
			return STRING;
		}
		if (note.palmMute != null && note.palmMute == 1) {
			return PALM;
		}

		return NONE;
	}

	public static Mute fromArrangmentChord(final ArrangementChord chord) {
		if (chord.fretHandMute != null && chord.fretHandMute == 1) {
			return STRING;
		}
		if (chord.palmMute != null && chord.palmMute == 1) {
			return PALM;
		}

		return NONE;
	}
}
