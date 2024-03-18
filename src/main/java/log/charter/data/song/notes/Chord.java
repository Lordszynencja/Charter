package log.charter.data.song.notes;

import static java.lang.Math.min;
import static log.charter.util.CollectionUtils.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.ChordTemplate;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.position.FractionalPosition;
import log.charter.data.song.position.fractional.IConstantFractionalPositionWithEnd;
import log.charter.io.rsc.xml.converters.ChordConverter;

@XStreamAlias("chord")
@XStreamConverter(ChordConverter.class)
public class Chord extends GuitarSound {
	public enum ChordNotesVisibility {
		NONE, NOTES, TAILS;
	}

	private int templateId;
	public boolean splitIntoNotes = false;
	public boolean forceNoNotes = false;
	public Map<Integer, ChordNote> chordNotes = new HashMap<>();

	public Chord(final int templateId) {
		this.templateId = templateId;
	}

	public Chord(final FractionalPosition position, final int templateId) {
		super(position);
		this.templateId = templateId;
	}

	public Chord(final FractionalPosition position, final int templateId, final ChordTemplate template) {
		super(position);
		updateTemplate(templateId, template);
	}

	public Chord(final Chord other) {
		super(other);
		templateId = other.templateId;
		accent = other.accent;
		splitIntoNotes = other.splitIntoNotes;
		forceNoNotes = other.forceNoNotes;
		chordNotes = map(other.chordNotes, i -> i, n -> new ChordNote(this, n));
	}

	public Chord(final Note note, final int templateId, final ChordTemplate template) {
		super(note);
		chordNotes.put(note.string, new ChordNote(this, note));
		updateTemplate(templateId, template);
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
		updateTemplate(template);
	}

	public void updateTemplate(final ChordTemplate template) {
		final Mute mute = chordNotesValue(n -> n.mute, Mute.NONE);
		final HOPO hopo = chordNotesValue(n -> n.hopo, HOPO.NONE);
		final FractionalPosition endPosition = endPosition();
		final boolean linkNext = chordNotesValue(n -> n.linkNext, false);
		final boolean tremolo = chordNotesValue(n -> n.tremolo, false);
		final boolean vibrato = chordNotesValue(n -> n.vibrato, false);

		for (final Integer string : template.frets.keySet()) {
			if (!chordNotes.containsKey(string)) {
				final ChordNote chordNote = new ChordNote(this, endPosition);
				chordNote.mute = mute;
				chordNote.hopo = hopo;
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
					|| !chordNote.bendValues.isEmpty() || chordNote.endPosition().compareTo(this.position()) > 0) {
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

	public ChordNotesVisibility chordNotesVisibility(final boolean shouldAddNotesByDefault) {
		if (forceNoNotes) {
			return ChordNotesVisibility.NONE;
		}

		final ChordNotesVisibility baseVisibility = chordNotesVisibility();
		if (baseVisibility != ChordNotesVisibility.NONE) {
			return baseVisibility;
		}

		return (shouldAddNotesByDefault && !fullyMuted()) ? ChordNotesVisibility.NOTES : ChordNotesVisibility.NONE;
	}

	public boolean linkNext() {
		return chordNotes.values().stream().anyMatch(n -> n.linkNext);
	}

	public void splitIntoNotes(final boolean value) {
		splitIntoNotes = value;
	}

	public void forceNoNotes(final boolean value) {
		forceNoNotes = value;
	}

	@Override
	public FractionalPosition endPosition() {
		return chordNotes.values().stream().map(n -> n.endPosition())
				.collect(Collectors.maxBy(FractionalPosition::compareTo)).orElse(this.position());
	}

	@Override
	public IConstantFractionalPositionWithEnd toFraction(final ImmutableBeatsMap beats) {
		return this;
	}

	@Override
	public String toString() {
		return "Chord [position=" + position()//
				+ ", templateId=" + templateId //
				+ ", splitIntoNotes=" + splitIntoNotes//
				+ ", forceNoNotes=" + forceNoNotes //
				+ ", chordNotes=" + chordNotes //
				+ ", accent=" + accent//
				+ ", ignore=" + ignore //
				+ ", passOtherNotes=" + passOtherNotes //
				+ "]";
	}

}
