package log.charter.song.notes;

import static java.lang.Math.min;
import static log.charter.util.Utils.mapInteger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import log.charter.io.rs.xml.song.ArrangementBendValue;
import log.charter.io.rs.xml.song.ArrangementChord;
import log.charter.io.rs.xml.song.ArrangementChordNote;
import log.charter.song.BendValue;
import log.charter.song.ChordTemplate;
import log.charter.song.enums.HOPO;
import log.charter.song.enums.Harmonic;
import log.charter.song.enums.Mute;
import log.charter.util.CollectionUtils.HashMap2;

public class Chord extends GuitarSound {
	public enum ChordNotesVisibility {
		NONE, NOTES, TAILS;
	}

	private int templateId;
	public boolean splitIntoNotes = false;
	public HashMap2<Integer, ChordNote> chordNotes = new HashMap2<>();

	public Chord(final int pos, final int templateId, final ChordTemplate template) {
		super(pos);
		this.templateId = templateId;
		updateChordNotes(template);
	}

	public Chord(final ArrangementChord arrangementChord, final ChordTemplate template) {
		super(arrangementChord.time, mapInteger(arrangementChord.accent), mapInteger(arrangementChord.ignore));
		templateId = arrangementChord.chordId;
		final Mute mute = Mute.fromArrangmentChord(arrangementChord);
		final boolean linkNext = mapInteger(arrangementChord.linkNext);

		if (arrangementChord.chordNotes != null) {
			for (final ArrangementChordNote arrangementNote : arrangementChord.chordNotes) {
				final ChordNote chordNote = new ChordNote();

				chordNote.length = arrangementNote.sustain == null ? 0 : arrangementNote.sustain;
				if (mapInteger(arrangementNote.mute)) {
					chordNote.mute = Mute.FULL;
				} else if (mapInteger(arrangementNote.palmMute)) {
					chordNote.mute = Mute.PALM;
				} else {
					chordNote.mute = mute;
				}

				if (arrangementNote.slideTo != null) {
					chordNote.slideTo = arrangementNote.slideTo;
				}
				if (arrangementNote.slideUnpitchTo != null) {
					chordNote.slideTo = arrangementNote.slideUnpitchTo;
					chordNote.unpitchedSlide = true;
				}

				chordNote.vibrato = mapInteger(arrangementNote.vibrato);
				chordNote.tremolo = mapInteger(arrangementNote.tremolo);

				if (mapInteger(arrangementNote.hammerOn)) {
					chordNote.hopo = HOPO.HAMMER_ON;
				}
				if (mapInteger(arrangementNote.pullOff)) {
					chordNote.hopo = HOPO.PULL_OFF;
				}
				if (mapInteger(arrangementNote.tap)) {
					chordNote.hopo = HOPO.TAP;
				}
				if (mapInteger(arrangementNote.harmonic)) {
					chordNote.harmonic = Harmonic.NORMAL;
				}
				if (mapInteger(arrangementNote.harmonicPinch)) {
					chordNote.harmonic = Harmonic.PINCH;
				}

				if (arrangementNote.bendValues != null && !arrangementNote.bendValues.list.isEmpty()) {
					for (final ArrangementBendValue bendValue : arrangementNote.bendValues.list) {
						chordNote.bendValues.add(new BendValue(bendValue, arrangementChord.time));
					}
				}

				chordNote.linkNext = linkNext || mapInteger(arrangementNote.linkNext);

				chordNotes.put(arrangementNote.string, chordNote);
			}
		}

		Set<Integer> existingChordNoteStrings;

		if (arrangementChord.chordNotes == null || arrangementChord.chordNotes.isEmpty()) {
			for (final Integer string : template.frets.keySet()) {
				if (!chordNotes.containsKey(string)) {
					final ChordNote chordNote = new ChordNote();
					chordNotes.put(string, chordNote);
				}
			}

			existingChordNoteStrings = new HashSet<>();
		} else {
			existingChordNoteStrings = arrangementChord.chordNotes.stream()//
					.map(arrangementChordNote -> arrangementChordNote.string)//
					.collect(Collectors.toSet());
		}

		updateChordNotes(template);

		chordNotes.forEach((string, chordNote) -> {
			if (!existingChordNoteStrings.contains(string)) {
				chordNote.mute = mute;
			}
		});
	}

	public Chord(final Chord other) {
		super(other);
		templateId = other.templateId;
		accent = other.accent;
		splitIntoNotes = other.splitIntoNotes;
		chordNotes = other.chordNotes.map(i -> i, ChordNote::new);
	}

	public Chord(final int templateId, final Note note, final ChordTemplate template) {
		super(note);
		this.templateId = templateId;
		chordNotes.put(note.string, new ChordNote(note));
		updateChordNotes(template);
	}

	private <T> List<T> chordNotesValues(final Function<ChordNote, T> getter) {
		return chordNotes.values().stream()//
				.map(getter)//
				.distinct()//
				.collect(Collectors.toList());
	}

	public <T> T chordNotesValue(final Function<ChordNote, T> getter, final T defaultValue) {
		final List<T> values = chordNotesValues(getter);
		return values.size() == 1 ? values.get(0) : defaultValue;
	}

	public boolean fullyMuted() {
		for (final ChordNote chordNote : chordNotes.values()) {
			if (chordNote.mute != Mute.FULL) {
				return false;
			}
		}

		return true;
	}

	public boolean palmMuted() {
		for (final ChordNote chordNote : chordNotes.values()) {
			if (chordNote.mute != Mute.PALM) {
				return false;
			}
		}

		return true;
	}

	public Integer slideTo() {
		Integer slideTo = null;
		for (final Entry<Integer, ChordNote> chordNote : chordNotes.entrySet()) {
			if (chordNote.getValue().slideTo != null) {
				slideTo = slideTo == null ? chordNote.getValue().slideTo : min(slideTo, chordNote.getValue().slideTo);
			}
		}

		return slideTo;
	}

	public int templateId() {
		return templateId;
	}

	public void updateTemplate(final int templateId, final ChordTemplate template) {
		this.templateId = templateId;
		updateChordNotes(template);
	}

	private void updateChordNotes(final ChordTemplate template) {
		final Mute mute = chordNotesValue(n -> n.mute, Mute.NONE);
		final HOPO hopo = chordNotesValue(n -> n.hopo, HOPO.NONE);
		final int length = length();
		final boolean linkNext = chordNotesValue(n -> n.linkNext, false);
		final boolean tremolo = chordNotesValue(n -> n.tremolo, false);
		final boolean vibrato = chordNotesValue(n -> n.vibrato, false);

		for (final Integer string : template.frets.keySet()) {
			if (!chordNotes.containsKey(string)) {
				final ChordNote chordNote = new ChordNote();
				chordNote.mute = mute;
				chordNote.hopo = hopo;
				chordNote.length = length;
				chordNote.linkNext = linkNext;
				chordNote.tremolo = tremolo;
				chordNote.vibrato = vibrato;

				chordNotes.put(string, chordNote);
			}
		}

		for (final Integer existingString : new ArrayList<>(chordNotes.keySet())) {
			if (!template.frets.containsKey(existingString)) {
				chordNotes.remove(existingString);
			}
		}
	}

	public ChordNotesVisibility chordNotesVisibility() {
		for (final ChordNote chordNote : chordNotes.values()) {
			if (chordNote.linkNext || chordNote.slideTo != null || chordNote.tremolo || chordNote.vibrato
					|| !chordNote.bendValues.isEmpty() || chordNote.length > 0) {
				return ChordNotesVisibility.TAILS;
			}
		}
		if (splitIntoNotes) {
			return ChordNotesVisibility.NOTES;
		}
		if (fullyMuted() || palmMuted()) {
			return ChordNotesVisibility.NONE;
		}

		for (final ChordNote chordNote : chordNotes.values()) {
			if (chordNote.harmonic != Harmonic.NONE || chordNote.hopo != HOPO.NONE || chordNote.mute != Mute.NONE) {
				return ChordNotesVisibility.NOTES;
			}
		}

		return ChordNotesVisibility.NONE;
	}

	public ChordNotesVisibility chordNotesVisibility(final boolean forceAddNotes) {
		final ChordNotesVisibility baseVisibility = chordNotesVisibility();
		if (baseVisibility != ChordNotesVisibility.NONE) {
			return baseVisibility;
		}

		return (forceAddNotes && !fullyMuted()) ? ChordNotesVisibility.NOTES : ChordNotesVisibility.NONE;
	}

	@Override
	public int length() {
		return chordNotes.values().stream().map(n -> n.length).collect(Collectors.maxBy(Integer::compareTo)).orElse(0);
	}

	@Override
	public void length(final int newLength) {
	}

	public boolean linkNext() {
		return chordNotes.values().stream().anyMatch(n -> n.linkNext);
	}

}
