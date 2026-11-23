package log.charter.io.rs.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.rs.xml.song.ArrangementTuning;

public class ArrangementTuningConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(ArrangementTuning.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final ArrangementTuning tuning = (ArrangementTuning) source;

		writer.addAttribute("extendedRange", tuning.extendedRange ? "1" : "0");
		for (int i = 0; i < tuning.tuningValues.length; i++) {
			writer.addAttribute("string" + i, "" + tuning.tuningValues[i]);
		}
		writer.addAttribute("strings", "" + tuning.strings);
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final int[] tuningValues;
		int strings;
		final boolean extendedRange = "1".equals(reader.getAttribute("extendedRange"));

		try {
			strings = Integer.parseInt(reader.getAttribute("strings"));
		} catch (NullPointerException | NumberFormatException e) {
			strings = 6;
		}

		tuningValues = new int[strings];
		for (int i = 0; i < strings; i++) {
			try {
				tuningValues[i] = Integer.parseInt(reader.getAttribute("string" + i));
			} catch (NullPointerException | NumberFormatException e) {
			}
		}

		return new ArrangementTuning(tuningValues, strings, extendedRange);
	}

}
