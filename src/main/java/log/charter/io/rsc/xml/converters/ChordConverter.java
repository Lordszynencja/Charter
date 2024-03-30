package log.charter.io.rsc.xml.converters;

import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.data.song.position.FractionalPosition;
import log.charter.io.rsc.xml.converters.BendValuesConverter.TemporaryBendValue;
import log.charter.util.CollectionUtils;

public class ChordConverter implements Converter {
	private static class TemporaryChordNote extends ChordNote {
		private final int length;

		public TemporaryChordNote(final Chord parent, final int length) {
			super(parent);
			this.length = length;
		}

		public ChordNote transform(final ImmutableBeatsMap beats, final int chordPosition) {
			this.endPosition(FractionalPosition.fromTimeRounded(beats, chordPosition + length));

			CollectionUtils.transform(bendValues, TemporaryBendValue.class, b -> b.transform(beats, chordPosition));

			return new ChordNote(parent, this);
		}
	}

	public static class TemporaryChord extends Chord {
		private final int position;

		public TemporaryChord(final int templateId, final int position) {
			super(templateId);
			this.position = position;
		}

		public Chord transform(final ImmutableBeatsMap beats) {
			this.position(FractionalPosition.fromTimeRounded(beats, position));

			for (final int string : chordNotes.keySet()) {
				final ChordNote chordNote = chordNotes.get(string);
				if (chordNote instanceof TemporaryChordNote) {
					chordNotes.put(string, ((TemporaryChordNote) chordNote).transform(beats, position));
				}
			}

			return new Chord(this);
		}

		@Override
		public String toString() {
			return "TemporaryChord [position=" + position//
					+ ", templateId=" + templateId() //
					+ ", splitIntoNotes=" + splitIntoNotes//
					+ ", forceNoNotes=" + forceNoNotes //
					+ ", chordNotes=" + chordNotes //
					+ ", accent=" + accent//
					+ ", ignore=" + ignore //
					+ ", passOtherNotes=" + passOtherNotes //
					+ "]";
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Chord.class);
	}

	private void writeBoolean(final HierarchicalStreamWriter writer, final String name, final boolean value) {
		if (!value) {
			return;
		}

		writer.addAttribute(name, "T");
	}

	private void writeBendValues(final HierarchicalStreamWriter writer, final String name,
			final List<BendValue> bendValues) {
		final String bendValuesString = BendValuesConverter.convertToString(bendValues);
		if (bendValuesString == null) {
			return;
		}

		writer.addAttribute(name, bendValuesString);
	}

	private void writeChordNote(final HierarchicalStreamWriter writer, final int string, final ChordNote note) {
		writer.startNode("chordNote");

		writer.addAttribute("string", string + "");
		writer.addAttribute("ep", note.endPosition().asString());

		if (note.mute != Mute.NONE) {
			writer.addAttribute("mute", note.mute.name());
		}
		if (note.hopo != HOPO.NONE) {
			writer.addAttribute("hopo", note.hopo.name());
		}
		if (note.harmonic != Harmonic.NONE) {
			writer.addAttribute("harmonic", note.harmonic.name());
		}
		writeBoolean(writer, "vibrato", note.vibrato);
		writeBoolean(writer, "tremolo", note.tremolo);
		writeBoolean(writer, "linkNext", note.linkNext);
		if (note.slideTo != null) {
			writer.addAttribute("slideTo", note.slideTo + "");
			writeBoolean(writer, "unpitchedSlide", note.unpitchedSlide);
		}
		writeBendValues(writer, "bendValues", note.bendValues);

		writer.endNode();
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Chord chord = (Chord) source;
		writer.addAttribute("p", chord.position().asString());
		writer.addAttribute("templateId", chord.templateId() + "");
		writeBoolean(writer, "splitIntoNotes", chord.splitIntoNotes);
		writeBoolean(writer, "forceNoNotes", chord.forceNoNotes);
		writeBoolean(writer, "accent", chord.accent);
		writeBoolean(writer, "ignore", chord.ignore);
		writeBoolean(writer, "crazy", chord.passOtherNotes);

		chord.chordNotes.forEach((string, chordNote) -> { writeChordNote(writer, string, chordNote); });
	}

	private boolean readBoolean(final String s) {
		return s == null ? false : "T".equals(s);
	}

	private ChordNote generateChordNoteFromPosition(final HierarchicalStreamReader reader, final Chord chord) {
		final String endPositionString = reader.getAttribute("ep");
		if (endPositionString != null) {
			return new ChordNote(chord, FractionalPosition.fromString(endPositionString));
		}

		final String lengthString = reader.getAttribute("length");
		final int length = lengthString == null || lengthString.isBlank() ? 0 : Integer.valueOf(lengthString);
		return new TemporaryChordNote(chord, length);
	}

	private void readChordNote(final HierarchicalStreamReader reader, final Chord chord) {
		reader.moveDown();

		final ChordNote chordNote = generateChordNoteFromPosition(reader, chord);

		final int string = Integer.valueOf(reader.getAttribute("string"));
		chord.chordNotes.put(string, chordNote);

		final String muteValue = reader.getAttribute("mute");
		if (muteValue != null) {
			chordNote.mute = Mute.valueOf(muteValue);
		}
		final String hopoValue = reader.getAttribute("hopo");
		if (hopoValue != null) {
			chordNote.hopo = HOPO.valueOf(hopoValue);
		}
		final String harmonicValue = reader.getAttribute("harmonic");
		if (harmonicValue != null) {
			chordNote.harmonic = Harmonic.valueOf(harmonicValue);
		}
		chordNote.vibrato = readBoolean(reader.getAttribute("vibrato"));
		chordNote.tremolo = readBoolean(reader.getAttribute("tremolo"));
		chordNote.linkNext = readBoolean(reader.getAttribute("linkNext"));
		final String slideTo = reader.getAttribute("slideTo");
		if (slideTo != null) {
			chordNote.slideTo = Integer.valueOf(slideTo);
			chordNote.unpitchedSlide = readBoolean(reader.getAttribute("unpitchedSlide"));
		}

		chordNote.bendValues = BendValuesConverter.convertFromString(reader.getAttribute("bendValues"));

		reader.moveUp();
	}

	private Chord generateChordFromPosition(final HierarchicalStreamReader reader, final int templateId) {
		String positionString = reader.getAttribute("p");
		if (positionString != null) {
			return new Chord(FractionalPosition.fromString(positionString), templateId);
		}

		positionString = reader.getAttribute("position");
		final int position = positionString == null || positionString.isBlank() ? 0 : Integer.valueOf(positionString);
		return new TemporaryChord(templateId, position);
	}

	@Override
	public Chord unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final int templateId = Integer.valueOf(reader.getAttribute("templateId"));
		final Chord chord = generateChordFromPosition(reader, templateId);

		chord.splitIntoNotes = readBoolean(reader.getAttribute("splitIntoNotes"));
		chord.forceNoNotes = readBoolean(reader.getAttribute("forceNoNotes"));
		chord.accent = readBoolean(reader.getAttribute("accent"));
		chord.ignore = readBoolean(reader.getAttribute("ignore"));
		chord.passOtherNotes = readBoolean(reader.getAttribute("crazy"));

		while (reader.hasMoreChildren()) {
			readChordNote(reader, chord);
		}

		return chord;
	}

}
