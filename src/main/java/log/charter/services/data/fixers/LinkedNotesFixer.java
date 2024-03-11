package log.charter.services.data.fixers;

import static log.charter.data.song.notes.ChordOrNote.findNextSoundOnString;

import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.util.collections.ArrayList2;

public class LinkedNotesFixer {
	public static void fixLinkedNote(final CommonNote note, final int id, final ArrayList2<ChordOrNote> sounds) {
		final ChordOrNote nextSound = findNextSoundOnString(note.string(), id + 1, sounds);
		if (nextSound == null) {
			return;
		}

		if (nextSound.isChord()) {
			nextSound.chord().splitIntoNotes = true;
		}

		note.length(nextSound.position() - note.position() - 1);
	}
}
