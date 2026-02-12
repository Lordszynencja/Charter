package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.data.song.configs.Tuning;
import log.charter.data.song.configs.Tuning.TuningType;

public class TuningConverter implements Converter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Tuning.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Tuning tuning = (Tuning) source;
		writer.addAttribute("type", tuning.tuningType.name());
		writer.addAttribute("strings", tuning.strings() + "");

		if (tuning.tuningType != TuningType.CUSTOM) {
			return;
		}

		final int[] tuningValues = tuning.getTuningRaw();
		final String[] valueStrings = new String[tuningValues.length];
		for (int i = 0; i < tuningValues.length; i++) {
			valueStrings[i] = tuningValues[i] + "";
		}

		writer.addAttribute("tuningValues", String.join(",", valueStrings));
	}

	private TuningType readTuningType(final String type) {
		if ("F_STANDARD".equals(type)) {
			return TuningType.LOW_F_STANDARD;
		}
		if ("C_SHARP_STANDARD".equals(type)) {
			return TuningType.D_FLAT_STANDARD;
		}
		if ("C_SHARP_DROP_B".equals(type)) {
			return TuningType.D_FLAT_DROP_B;
		}
		if ("A_SHARP_STANDARD".equals(type)) {
			return TuningType.B_FLAT_STANDARD;
		}
		if ("G_SHARP_STANDARD".equals(type)) {
			return TuningType.A_FLAT_STANDARD;
		}
		if ("F_SHARP_STANDARD".equals(type)) {
			return TuningType.G_FLAT_STANDARD;
		}
		if ("B_FLAT_DROP_G".equals(type)) {
			return TuningType.B_FLAT_DROP_A_FLAT;
		}
		if ("A_DROP_G_FLAT".equals(type)) {
			return TuningType.A_DROP_G;
		}

		return TuningType.valueOf(type);
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		TuningType type = readTuningType(reader.getAttribute("type"));
		final int strings = Integer.valueOf(reader.getAttribute("strings"));
		if (type != TuningType.CUSTOM) {
			return new Tuning(type, strings);
		}

		final int[] tuning = new int[strings];
		final String tuningValues = reader.getAttribute("tuningValues");
		if (tuningValues != null) {
			final String[] values = tuningValues.split(",");
			for (int i = 0; i < strings; i++) {
				tuning[i] = Integer.valueOf(values[i]);
			}
		}

		type = TuningType.fromTuning(tuning);
		if (type != TuningType.CUSTOM) {
			return new Tuning(type, strings);
		}
		return new Tuning(type, strings, tuning);
	}

}
