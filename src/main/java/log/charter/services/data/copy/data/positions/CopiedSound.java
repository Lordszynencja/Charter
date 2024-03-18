package log.charter.services.data.copy.data.positions;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordOrNote;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;

public interface CopiedSound extends Copied<ChordOrNote> {
	public static CopiedSound copy(final FractionalPosition basePosition, final ChordOrNote sound) {
		return sound.isChord() ? new CopiedSoundChord(basePosition, sound) : new CopiedSoundNote(basePosition, sound);
	}

	public static class CopiedChord extends CopiedFractionalPosition<Chord> {
		private final Chord chord;

		public CopiedChord(final FractionalPosition basePosition, final Chord item) {
			super(basePosition, item);
			chord = item;
		}

		@Override
		public Chord prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
				final boolean convertFromBeats) {
			return chord;
		}
	}

	@XStreamAlias("copiedChord")
	public static class CopiedSoundChord implements CopiedSound {
		private final CopiedChord chord;

		public CopiedSoundChord(final FractionalPosition basePosition, final ChordOrNote item) {
			chord = new CopiedChord(basePosition, item.chord());
		}

		@Override
		public ChordOrNote prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
				final boolean convertFromBeats) {
			return ChordOrNote.from(chord.getValue(beats, basePosition, convertFromBeats));
		}
	}

	public static class CopiedNote extends CopiedFractionalPositionWithEnd<Note> {
		private final Note note;

		public CopiedNote(final FractionalPosition basePosition, final Note item) {
			super(basePosition, item);
			note = item;
		}

		@Override
		public Note prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
				final boolean convertFromBeats) {
			return note;
		}
	}

	@XStreamAlias("copiedNote")
	public static class CopiedSoundNote implements CopiedSound {
		private final CopiedNote note;

		public CopiedSoundNote(final FractionalPosition basePosition, final ChordOrNote item) {
			note = new CopiedNote(basePosition, item.note());
		}

		@Override
		public ChordOrNote prepareValue(final ImmutableBeatsMap beats, final FractionalPosition basePosition,
				final boolean convertFromBeats) {
			return ChordOrNote.from(note.getValue(beats, basePosition, convertFromBeats));
		}
	}
}
