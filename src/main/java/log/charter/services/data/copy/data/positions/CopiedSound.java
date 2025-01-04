package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.services.data.copy.data.positions.CopiedSound.CopiedSoundChord;
import log.charter.services.data.copy.data.positions.CopiedSound.CopiedSoundNote;

@XStreamInclude({ CopiedSoundChord.class, CopiedSoundNote.class })
public interface CopiedSound extends Copied<ChordOrNote> {
	public static CopiedSound copy(final FractionalPosition basePosition, final ChordOrNote sound) {
		return sound.isChord() ? new CopiedSoundChord(basePosition, sound) : new CopiedSoundNote(basePosition, sound);
	}

	@XStreamAlias("copiedChord")
	public static class CopiedSoundChord implements CopiedSound {
		private final Chord chord;

		public CopiedSoundChord(final FractionalPosition basePosition, final ChordOrNote item) {
			chord = new Chord(item.chord());
			chord.position(chord.position().add(basePosition.negate()));
			chord.chordNotes.values()
					.forEach(chordNote -> {
						chordNote.endPosition(chordNote.endPosition().add(basePosition.negate()));
						chordNote.bendValues.forEach(bend -> bend.position(bend.position().add(basePosition.negate())));
					});
		}

		@Override
		public ChordOrNote prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
				final boolean convertFromBeats) {
			chord.position(chord.position().add(basePosition));
			chord.chordNotes.values()
					.forEach(chordNote -> {
						chordNote.endPosition(chordNote.endPosition().add(basePosition));
						chordNote.bendValues.forEach(bend -> bend.position(bend.position().add(basePosition)));
					});

			return ChordOrNote.from(chord);
		}
	}

	@XStreamAlias("copiedNote")
	public static class CopiedSoundNote implements CopiedSound {
		private final Note note;

		public CopiedSoundNote(final FractionalPosition basePosition, final ChordOrNote item) {
			note = new Note(item.note());
			note.position(note.position().add(basePosition.negate()));
			note.endPosition(note.endPosition().add(basePosition.negate()));
			note.bendValues.forEach(bend -> bend.position(bend.position().add(basePosition.negate())));
		}

		@Override
		public ChordOrNote prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
				final boolean convertFromBeats) {
			note.position(note.position().add(basePosition));
			note.endPosition(note.endPosition().add(basePosition));
			note.bendValues.forEach(bend -> bend.position(bend.position().add(basePosition)));
			return ChordOrNote.from(note);
		}
	}
}
