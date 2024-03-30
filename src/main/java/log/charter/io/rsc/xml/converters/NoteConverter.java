package log.charter.io.rsc.xml.converters;

import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.BeatsMap.ImmutableBeatsMap;
import log.charter.data.song.BendValue;
import log.charter.data.song.enums.BassPickingTechnique;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Note;
import log.charter.data.song.position.FractionalPosition;
import log.charter.io.rsc.xml.converters.BendValuesConverter.TemporaryBendValue;
import log.charter.util.CollectionUtils;

public class NoteConverter implements Converter {
	public static class TemporaryNote extends Note {
		private final int position;
		private final int endPosition;

		public TemporaryNote(final int position, final int endPosition) {
			this.position = position;
			this.endPosition = endPosition;
		}

		public Note transform(final ImmutableBeatsMap beats) {
			this.position(FractionalPosition.fromTimeRounded(beats, position));
			this.endPosition(FractionalPosition.fromTimeRounded(beats, endPosition));

			CollectionUtils.transform(bendValues, TemporaryBendValue.class, b -> b.transform(beats, position));

			return new Note(this);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Note.class);
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

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Note note = (Note) source;

		writer.addAttribute("p", note.position().asString());
		writer.addAttribute("ep", note.endPosition().asString());
		writer.addAttribute("string", note.string + "");
		writer.addAttribute("fret", note.fret + "");
		writeBoolean(writer, "accent", note.accent);
		writeBoolean(writer, "ignore", note.ignore);
		writeBoolean(writer, "crazy", note.passOtherNotes);
		if (note.bassPicking != BassPickingTechnique.NONE) {
			writer.addAttribute("bassPicking", note.bassPicking.name());
		}
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
	}

	private Note generateNoteFromPosition(final HierarchicalStreamReader reader) {
		final String positionString = reader.getAttribute("position");
		if (positionString != null && !positionString.isBlank()) {
			final int position = Integer.valueOf(positionString);
			final String lengthString = reader.getAttribute("length");
			final int length = lengthString == null || lengthString.isBlank() ? 0 : Integer.valueOf(lengthString);
			return new TemporaryNote(position, position + length);
		}

		return new Note(FractionalPosition.fromString(reader.getAttribute("p")),
				FractionalPosition.fromString(reader.getAttribute("ep")));
	}

	private boolean readBoolean(final String s) {
		return s == null ? false : "T".equals(s);
	}

	@Override
	public Note unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final Note note = generateNoteFromPosition(reader);
		note.string = Integer.valueOf(reader.getAttribute("string"));
		note.fret = Integer.valueOf(reader.getAttribute("fret"));

		note.accent = readBoolean(reader.getAttribute("accent"));
		note.ignore = readBoolean(reader.getAttribute("ignore"));
		note.passOtherNotes = readBoolean(reader.getAttribute("crazy"));

		final String bassPickingValue = reader.getAttribute("bassPicking");
		if (bassPickingValue != null) {
			note.bassPicking = BassPickingTechnique.valueOf(bassPickingValue);
		}
		final String muteValue = reader.getAttribute("mute");
		if (muteValue != null) {
			note.mute = Mute.valueOf(muteValue);
		}
		final String hopoValue = reader.getAttribute("hopo");
		if (hopoValue != null) {
			note.hopo = HOPO.valueOf(hopoValue);
		}
		final String harmonicValue = reader.getAttribute("harmonic");
		if (harmonicValue != null) {
			note.harmonic = Harmonic.valueOf(harmonicValue);
		}
		note.vibrato = readBoolean(reader.getAttribute("vibrato"));
		note.tremolo = readBoolean(reader.getAttribute("tremolo"));
		note.linkNext = readBoolean(reader.getAttribute("linkNext"));
		final String slideTo = reader.getAttribute("slideTo");
		if (slideTo != null) {
			note.slideTo = Integer.valueOf(slideTo);
			note.unpitchedSlide = readBoolean(reader.getAttribute("unpitchedSlide"));
		}

		note.bendValues = BendValuesConverter.convertFromString(reader.getAttribute("bendValues"));

		return note;
	}

}
