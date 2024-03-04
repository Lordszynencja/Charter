package log.charter.song.notes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rsc.xml.converters.ChordOrNoteConverter;
import log.charter.song.ChordTemplate;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("sound")
@XStreamConverter(ChordOrNoteConverter.class)
public interface ChordOrNote extends IPositionWithLength {
	public static ChordOrNote findNextSoundOnString(final int string, final int startFromId,
			final ArrayList2<ChordOrNote> sounds) {
		for (int i = startFromId; i < sounds.size(); i++) {
			final ChordOrNote sound = sounds.get(i);
			if (sound.isNote()) {
				if (sound.note().string == string) {
					return sound;
				}
			} else if (sound.chord().chordNotes.containsKey(string)) {
				return sound;
			}
		}

		return null;
	}

	public static ChordOrNote findPreviousSoundOnString(final int string, final int startFromId,
			final ArrayList2<ChordOrNote> sounds) {
		for (int i = startFromId; i >= 0; i--) {
			final ChordOrNote sound = sounds.get(i);
			if (sound.isNote()) {
				if (sound.note().string == string) {
					return sound;
				}
			} else if (sound.chord().chordNotes.containsKey(string)) {
				return sound;
			}
		}

		return null;
	}

	public static boolean isLinkedToPrevious(final int string, final int id, final ArrayList2<ChordOrNote> sounds) {
		final ChordOrNote previousSound = findPreviousSoundOnString(string, id - 1, sounds);
		return previousSound != null && previousSound.linkNext(string);
	}

	public static boolean isLinkedToPrevious(final ChordOrNote sound, final int id,
			final ArrayList2<ChordOrNote> sounds) {
		if (sound.isNote()) {
			return isLinkedToPrevious(sound.note().string, id, sounds);
		}

		for (final int string : sound.chord().chordNotes.keySet()) {
			if (!isLinkedToPrevious(string, id, sounds)) {
				return false;
			}
		}
		return true;
	}

	public static class ChordOrNoteForNote implements ChordOrNote {
		private final Note note;

		public ChordOrNoteForNote(final Note note) {
			this.note = note;
		}

		@Override
		public boolean isNote() {
			return true;
		}

		@Override
		public Note note() {
			return note;
		}

		@Override
		public GuitarSound asGuitarSound() {
			return note;
		}

		@Override
		public int position() {
			return asGuitarSound().position();
		}

		@Override
		public void position(final int newPosition) {
			asGuitarSound().position(newPosition);
		}

		@Override
		public int length() {
			return asGuitarSound().length();
		}

		@Override
		public void length(final int newLength) {
			asGuitarSound().length(newLength);
		}

		@Override
		public boolean linkNext(final int string) {
			return note.string == string && note.linkNext;
		}

		@Override
		public ChordOrNote turnToNote(final ChordTemplate chordTemplate) {
			return this;
		}

		@Override
		public ChordOrNote turnToChord(final int chordId, final ChordTemplate chordTemplate) {
			return new ChordOrNoteForChord(new Chord(chordId, note, chordTemplate));
		}

		@Override
		public Optional<NoteInterface> getString(final int string) {
			return note.string == string ? Optional.of(note) : Optional.empty();
		}

		@Override
		public Stream<? extends NoteInterface> noteInterfaces() {
			return Stream.of(note);
		}

		@Override
		public Stream<CommonNote> notes() {
			return Stream.of(CommonNote.create(note));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final List<ChordTemplate> chordTemplates) {
			return Stream.of(CommonNoteWithFret.create(note));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final ChordTemplate chordTemplate) {
			return Stream.of(CommonNoteWithFret.create(note));
		}
	}

	public static class ChordOrNoteForChord implements ChordOrNote {
		private final Chord chord;

		public ChordOrNoteForChord(final Chord chord) {
			this.chord = chord;
		}

		@Override
		public boolean isChord() {
			return true;
		}

		@Override
		public Chord chord() {
			return chord;
		}

		@Override
		public GuitarSound asGuitarSound() {
			return chord;
		}

		@Override
		public int position() {
			return asGuitarSound().position();
		}

		@Override
		public void position(final int newPosition) {
			asGuitarSound().position(newPosition);
		}

		@Override
		public int length() {
			return asGuitarSound().length();
		}

		@Override
		public void length(final int newLength) {
			asGuitarSound().length(newLength);
		}

		@Override
		public boolean linkNext(final int string) {
			return chord.chordNotes.containsKey(string) && chord.chordNotes.get(string).linkNext;
		}

		@Override
		public ChordOrNote turnToNote(final ChordTemplate chordTemplate) {
			return new ChordOrNoteForNote(new Note(chord, chordTemplate));
		}

		@Override
		public ChordOrNote turnToChord(final int chordId, final ChordTemplate chordTemplate) {
			return this;
		}

		@Override
		public Optional<NoteInterface> getString(final int string) {
			return Optional.ofNullable(chord.chordNotes.get(string));
		}

		@Override
		public Stream<? extends NoteInterface> noteInterfaces() {
			return chord.chordNotes.values().stream();
		}

		@Override
		public Stream<CommonNote> notes() {
			return chord.chordNotes.keySet().stream()//
					.map(string -> CommonNote.create(chord, string));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final List<ChordTemplate> chordTemplates) {
			return notesWithFrets(chordTemplates.get(chord.templateId()));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final ChordTemplate chordTemplate) {
			return chord.chordNotes.keySet().stream()//
					.map(string -> CommonNoteWithFret.create(chord, string, chordTemplate.frets.get(string)));
		}
	}

	public static ChordOrNote from(final Chord chord) {
		return new ChordOrNoteForChord(chord);
	}

	public static ChordOrNote from(final Note note) {
		return new ChordOrNoteForNote(note);
	}

	public static ChordOrNote from(final ChordOrNote other) {
		if (other.isNote()) {
			return from(new Note(other.note()));
		}
		if (other.isChord()) {
			return from(new Chord(other.chord()));
		}

		throw new IllegalArgumentException("unknown note type");
	}

	default Chord chord() {
		return null;
	}

	default Note note() {
		return null;
	}

	default boolean isChord() {
		return false;
	}

	default boolean isNote() {
		return false;
	}

	GuitarSound asGuitarSound();

	boolean linkNext(final int string);

	ChordOrNote turnToNote(final ChordTemplate chordTemplate);

	ChordOrNote turnToChord(final int chordId, final ChordTemplate chordTemplate);

	Optional<NoteInterface> getString(int string);

	Stream<? extends NoteInterface> noteInterfaces();

	Stream<CommonNote> notes();

	Stream<CommonNoteWithFret> notesWithFrets(List<ChordTemplate> chordTemplates);

	Stream<CommonNoteWithFret> notesWithFrets(ChordTemplate chordTemplate);
}
