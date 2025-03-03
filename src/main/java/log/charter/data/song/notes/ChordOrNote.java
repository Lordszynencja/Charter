package log.charter.data.song.notes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.notes.ChordOrNote.ChordOrNoteForChord;
import log.charter.data.song.notes.ChordOrNote.ChordOrNoteForNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.data.song.position.fractional.IFractionalPosition;
import log.charter.io.rsc.xml.converters.ChordOrNoteConverter;
import log.charter.io.rsc.xml.converters.ChordOrNoteForChordConverter;
import log.charter.io.rsc.xml.converters.ChordOrNoteForNoteConverter;
import log.charter.util.collections.Pair;

@XStreamAlias("sound")
@XStreamConverter(ChordOrNoteConverter.class)
@XStreamInclude({ ChordOrNoteForChord.class, ChordOrNoteForNote.class })
public interface ChordOrNote extends IFractionalPosition, IConstantFractionalPositionWithEnd {
	public static Pair<Integer, ChordOrNote> findNextSoundWithIdOnString(final int string, final int startFromId,
			final List<ChordOrNote> sounds) {
		for (int i = startFromId; i < sounds.size(); i++) {
			final ChordOrNote sound = sounds.get(i);
			if (sound.isNote()) {
				if (sound.note().string == string) {
					return new Pair<>(i, sound);
				}
			} else if (sound.chord().chordNotes.containsKey(string)) {
				return new Pair<>(i, sound);
			}
		}

		return null;
	}

	public static ChordOrNote findNextSoundOnString(final int string, final int startFromId,
			final List<ChordOrNote> sounds) {
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

	public static Pair<Integer, ChordOrNote> findPreviousSoundWithIdOnString(final int string, final int startFromId,
			final List<ChordOrNote> sounds) {
		for (int i = startFromId; i >= 0; i--) {
			final ChordOrNote sound = sounds.get(i);
			if (sound.isNote()) {
				if (sound.note().string == string) {
					return new Pair<>(i, sound);
				}
			} else if (sound.chord().chordNotes.containsKey(string)) {
				return new Pair<>(i, sound);
			}
		}

		return null;
	}

	public static ChordOrNote findPreviousSoundOnString(final int string, final int startFromId,
			final List<ChordOrNote> sounds) {
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

	public static boolean isLinkedToPrevious(final int string, final int id, final List<ChordOrNote> sounds) {
		final ChordOrNote previousSound = findPreviousSoundOnString(string, id - 1, sounds);
		return previousSound != null && previousSound.linkNext(string);
	}

	public static boolean isLinkedToPrevious(final ChordOrNote sound, final int id, final List<ChordOrNote> sounds) {
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
		public FractionalPosition position() {
			return chord.position();
		}

		@Override
		public void position(final FractionalPosition newPosition) {
			chord.position(newPosition);
		}

		@Override
		public FractionalPosition endPosition() {
			return chord.endPosition();
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
					.map(string -> new CommonNoteWithFret(chord, string, chordTemplate.frets.get(string),
							chordTemplate.fingers.get(string)));
		}

		@Override
		public Optional<CommonNoteWithFret> noteWithFret(final int string, final List<ChordTemplate> chordTemplates) {
			return noteWithFret(string, chordTemplates.get(chord.templateId()));
		}

		@Override
		public Optional<CommonNoteWithFret> noteWithFret(final int string, final ChordTemplate chordTemplate) {
			if (!chord.chordNotes.containsKey(string)) {
				return Optional.empty();
			}

			return Optional.of(new CommonNoteWithFret(chord, string, chordTemplate.frets.get(string),
					chordTemplate.fingers.get(string)));
		}

		@Override
		public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
			return chord;
		}

		@Override
		public String toString() {
			return "ChordOrNoteForChord[chord=" + chord.toString() + "]";
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
		public FractionalPosition position() {
			return note.position();
		}

		@Override
		public void position(final FractionalPosition newPosition) {
			note.position(newPosition);
		}

		@Override
		public FractionalPosition endPosition() {
			return note.endPosition();
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
			return new ChordOrNoteForChord(new Chord(note, chordId, chordTemplate));
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
		public Optional<CommonNoteWithFret> noteWithFret(final int string, final List<ChordTemplate> chordTemplates) {
			return getString(string).map(CommonNoteWithFret::new);
		}

		@Override
		public Optional<CommonNoteWithFret> noteWithFret(final int string, final ChordTemplate chordTemplate) {
			return getString(string).map(CommonNoteWithFret::new);
		}

		@Override
		public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
			return note;
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

	Optional<? extends CommonNoteWithFret> noteWithFret(int string, List<ChordTemplate> chordTemplates);

	Optional<? extends CommonNoteWithFret> noteWithFret(int string, ChordTemplate chordTemplate);

	@Override
	default boolean isFraction() {
		return true;
	}

	@Override
	default boolean isPosition() {
		return false;
	}

	@Override
	default void move(final FractionalPosition distance) {
		if (isNote()) {
			note().move(distance);
			return;
		}

		final Chord chord = chord();
		chord.move(distance);
		chord.chordNotes.values().forEach(n -> {
			n.endPosition(n.endPosition().add(distance));
			n.bendValues.forEach(p -> p.move(distance));
		});
	}

	@Override
	default void move(final int beats) {
		this.move(new FractionalPosition(beats));
	}
}
