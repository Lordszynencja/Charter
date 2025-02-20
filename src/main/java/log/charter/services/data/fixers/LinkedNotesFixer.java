package log.charter.services.data.fixers;

import static log.charter.data.song.notes.ChordOrNote.findNextSoundOnString;

import java.util.List;

import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.CommonNote;
import log.charter.data.song.notes.Note;

public class LinkedNotesFixer {
	public static void fixLinkedNote(final CommonNote note, final int id, final List<ChordOrNote> sounds) {
		final ChordOrNote nextSound = findNextSoundOnString(note.string(), id + 1, sounds);
		if (nextSound == null) {
			return;
		}

		if (nextSound.isChord()) {
			nextSound.chord().splitIntoNotes = true;
		}

		note.endPosition(nextSound.position());

		if (nextSound.isNote()) {
			final Note nextNote = nextSound.note();
			nextNote.accent = false;
			nextNote.bassPicking = BassPickingTechnique.NONE;
			nextNote.harmonic = note.harmonic();
			nextNote.hopo = HOPO.NONE;
			nextNote.mute = Mute.NONE;
		} else {
			final Chord chord = nextSound.chord();
			chord.accent = false;

			final ChordNote nextNote = chord.chordNotes.get(note.string());
			nextNote.bassPicking(BassPickingTechnique.NONE);
			nextNote.harmonic = note.harmonic();
			nextNote.hopo = HOPO.NONE;
			nextNote.mute = Mute.NONE;
		}
	}
}
