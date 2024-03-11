package log.charter.io.rsc.xml.converters;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.ChordTemplate;
import log.charter.util.collections.HashMap2;

public class ChordTemplateConverter implements Converter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(ChordTemplate.class);
	}

	private void writeMap(final HierarchicalStreamWriter writer, final String name,
			final HashMap2<Integer, Integer> map) {
		final List<String> values = new ArrayList<>();
		map.forEach((key, value) -> values.add(key + "=" + value));
		if (!values.isEmpty()) {
			writer.addAttribute(name, String.join(",", values));
		}
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final ChordTemplate chordTemplate = (ChordTemplate) source;

		writer.addAttribute("chordName", chordTemplate.chordName);
		if (chordTemplate.arpeggio) {
			writer.addAttribute("arpeggio", "T");
		}

		writeMap(writer, "fingers", chordTemplate.fingers);
		writeMap(writer, "frets", chordTemplate.frets);
	}

	private boolean readBoolean(final String s) {
		return s == null ? false : "T".equals(s);
	}

	private HashMap2<Integer, Integer> readMap(final String s) {
		final HashMap2<Integer, Integer> map = new HashMap2<>();

		if (s == null) {
			return map;
		}

		for (final String pair : s.split(",")) {
			final String[] pairValues = pair.split("=");
			map.put(Integer.valueOf(pairValues[0]), Integer.valueOf(pairValues[1]));
		}

		return map;
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final String chordName = reader.getAttribute("chordName");
		final boolean arpeggio = readBoolean(reader.getAttribute("arpeggio"));
		final HashMap2<Integer, Integer> fingers = readMap(reader.getAttribute("fingers"));
		final HashMap2<Integer, Integer> frets = readMap(reader.getAttribute("frets"));

		return new ChordTemplate(chordName, arpeggio, fingers, frets);
	}

}
