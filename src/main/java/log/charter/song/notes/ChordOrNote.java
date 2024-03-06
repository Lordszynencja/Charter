package log.charter.song.notes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rsc.xml.converters.ChordOrNoteConverter;
import log.charter.io.rsc.xml.converters.ChordOrNoteForChordConverter;
import log.charter.io.rsc.xml.converters.ChordOrNoteForNoteConverter;
import log.charter.song.ChordTemplate;
import log.charter.song.notes.ChordOrNote.ChordOrNoteForChord;
import log.charter.song.notes.ChordOrNote.ChordOrNoteForNote;
import log.charter.util.CollectionUtils.ArrayList2;

@XStreamAlias("sound")
@XStreamConverter(ChordOrNoteConverter.class)
@XStreamInclude({ ChordOrNoteForChord.class, ChordOrNoteForNote.class })
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

	@XStreamAlias("soundChord")
	@XStreamConverter(ChordOrNoteForChordConverter.class)
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
		public ChordOrNote asNote(final List<ChordTemplate> chordTemplates) {
			return new ChordOrNoteForNote(new Note(chord, chordTemplates.get(chord.templateId())));
		}

		@Override
		public ChordOrNote asChord(final int chordId, final ChordTemplate chordTemplate) {
			return this;
		}

		@Override
		public boolean hasString(final int string) {
			return chord.chordNotes.containsKey(string);
		}

		@Override
		public Optional<ChordNote> getString(final int string) {
			return Optional.ofNullable(chord.chordNotes.get(string));
		}

		@Override
		public Stream<ChordNote> noteInterfaces() {
			return chord.chordNotes.values().stream();
		}

		@Override
		public Stream<CommonNote> notes() {
			return chord.chordNotes.keySet().stream()//
					.map(string -> new CommonNote(chord, string));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final List<ChordTemplate> chordTemplates) {
			return notesWithFrets(chordTemplates.get(chord.templateId()));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final ChordTemplate chordTemplate) {
			return chord.chordNotes.keySet().stream()//
					.map(string -> new CommonNoteWithFret(chord, string, chordTemplate.frets.get(string)));
		}

		@Override
		public Optional<CommonNoteWithFret> noteWithFrets(final int string, final List<ChordTemplate> chordTemplates) {
			return noteWithFrets(string, chordTemplates.get(chord.templateId()));
		}

		@Override
		public Optional<CommonNoteWithFret> noteWithFrets(final int string, final ChordTemplate chordTemplate) {
			if (!chord.chordNotes.containsKey(string)) {
				return Optional.empty();
			}

			return Optional.of(new CommonNoteWithFret(chord, string, chordTemplate.frets.get(string)));
		}
	}

	@XStreamAlias("soundNote")
	@XStreamConverter(ChordOrNoteForNoteConverter.class)
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
		public ChordOrNote asNote(final List<ChordTemplate> chordTemplates) {
			return this;
		}

		@Override
		public ChordOrNote asChord(final int chordId, final ChordTemplate chordTemplate) {
			return new ChordOrNoteForChord(new Chord(chordId, note, chordTemplate));
		}

		@Override
		public boolean hasString(final int string) {
			return note.string == string;
		}

		@Override
		public Optional<Note> getString(final int string) {
			return note.string == string ? Optional.of(note) : Optional.empty();
		}

		@Override
		public Stream<Note> noteInterfaces() {
			return Stream.of(note);
		}

		@Override
		public Stream<CommonNote> notes() {
			return Stream.of(new CommonNote(note));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final List<ChordTemplate> chordTemplates) {
			return Stream.of(new CommonNoteWithFret(note));
		}

		@Override
		public Stream<CommonNoteWithFret> notesWithFrets(final ChordTemplate chordTemplate) {
			return Stream.of(new CommonNoteWithFret(note));
		}

		@Override
		public Optional<CommonNoteWithFret> noteWithFrets(final int string, final List<ChordTemplate> chordTemplates) {
			return getString(string).map(CommonNoteWithFret::new);
		}

		@Override
		public Optional<CommonNoteWithFret> noteWithFrets(final int string, final ChordTemplate chordTemplate) {
			return getString(string).map(CommonNoteWithFret::new);
		}
	}

	public static ChordOrNoteForChord from(final Chord chord) {
		return new ChordOrNoteForChord(chord);
	}

	public static ChordOrNoteForNote from(final Note note) {
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

	ChordOrNote asNote(final List<ChordTemplate> chordTemplates);

	ChordOrNote asChord(final int chordId, final ChordTemplate chordTemplate);

	boolean hasString(int string);

	Optional<? extends NoteInterface> getString(int string);

	Stream<? extends NoteInterface> noteInterfaces();

	Stream<? extends CommonNote> notes();

	Stream<? extends CommonNoteWithFret> notesWithFrets(List<ChordTemplate> chordTemplates);

	Stream<? extends CommonNoteWithFret> notesWithFrets(ChordTemplate chordTemplate);

	Optional<? extends CommonNoteWithFret> noteWithFrets(int string, List<ChordTemplate> chordTemplates);

	Optional<? extends CommonNoteWithFret> noteWithFrets(int string, ChordTemplate chordTemplate);
}
