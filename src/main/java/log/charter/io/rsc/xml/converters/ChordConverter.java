package log.charter.io.rsc.xml.converters;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.BendValue;
import log.charter.data.song.enums.HOPO;
import log.charter.data.song.enums.Harmonic;
import log.charter.data.song.enums.Mute;
import log.charter.data.song.notes.Chord;
import log.charter.data.song.notes.ChordNote;
import log.charter.util.CollectionUtils.ArrayList2;

public class ChordConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Chord.class);
	}

	private void writePositiveInteger(final HierarchicalStreamWriter writer, final String name, final int value) {
		if (value <= 0) {
			return;
		}

		writer.addAttribute(name, value + "");
	}

	private void writeBoolean(final HierarchicalStreamWriter writer, final String name, final boolean value) {
		if (!value) {
			return;
		}

		writer.addAttribute(name, "T");
	}

	private void writeBendValues(final HierarchicalStreamWriter writer, final String name,
			final ArrayList2<BendValue> bendValues) {
		if (bendValues.isEmpty()) {
			return;
		}

		final String bendValuesString = bendValues.stream()//
				.map(bendValue -> bendValue.position() + "=" + bendValue.bendValue.toString())//
				.collect(Collectors.joining(";"));

		writer.addAttribute(name, bendValuesString);
	}

	private void writeChordNote(final HierarchicalStreamWriter writer, final int string, final ChordNote note) {
		writer.startNode("chordNote");

		writer.addAttribute("string", string + "");
		writePositiveInteger(writer, "length", note.length);

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
		writer.addAttribute("position", chord.position() + "");
		writer.addAttribute("templateId", chord.templateId() + "");
		writeBoolean(writer, "splitIntoNotes", chord.splitIntoNotes);
		writeBoolean(writer, "forceNoNotes", chord.forceNoNotes);
		writeBoolean(writer, "accent", chord.accent);
		writeBoolean(writer, "ignore", chord.ignore);
		writeBoolean(writer, "crazy", chord.passOtherNotes);

		chord.chordNotes.forEach((string, chordNote) -> { writeChordNote(writer, string, chordNote); });
	}

	private int readPositiveInteger(final String s) {
		if (s == null) {
			return 0;
		}

		return Integer.valueOf(s);
	}

	private boolean readBoolean(final String s) {
		return s == null ? false : "T".equals(s);
	}

	private ArrayList2<BendValue> readBendValues(final String s) {
		final ArrayList2<BendValue> bendValues = new ArrayList2<>();

		if (s == null) {
			return bendValues;
		}

		for (final String pair : s.split(";")) {
			final String[] pairValues = pair.split("=");
			bendValues.add(new BendValue(Integer.valueOf(pairValues[0]), new BigDecimal(pairValues[1])));
		}

		return bendValues;
	}

	private void readChordNote(final HierarchicalStreamReader reader, final Chord chord) {
		reader.moveDown();

		final int string = Integer.valueOf(reader.getAttribute("string"));
		final ChordNote note = new ChordNote();
		chord.chordNotes.put(string, note);

		note.length = readPositiveInteger(reader.getAttribute("length"));

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

		note.bendValues = readBendValues(reader.getAttribute("bendValues"));

		reader.moveUp();
	}

	@Override
	public Chord unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final int position = Integer.valueOf(reader.getAttribute("position"));
		final int templateId = Integer.valueOf(reader.getAttribute("templateId"));
		final Chord chord = new Chord(position, templateId);

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
