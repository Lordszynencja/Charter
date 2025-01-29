package log.charter.io.gp.gp7.converters;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.Logger;
import log.charter.io.gp.gp5.data.GPDuration;
import log.charter.io.gp.gp7.data.GP7Rhythm;
import log.charter.io.gp.gp7.data.GP7Tuplet;

public class GP7RhythmConverter implements Converter {
	private static Map<String, GPDuration> durationsMap = new HashMap<>();
	static {
		durationsMap.put("Whole", GPDuration.NOTE_1);
		durationsMap.put("Half", GPDuration.NOTE_2);
		durationsMap.put("Quarter", GPDuration.NOTE_4);
		durationsMap.put("Eighth", GPDuration.NOTE_8);
		durationsMap.put("16th", GPDuration.NOTE_16);
		durationsMap.put("32nd", GPDuration.NOTE_32);
		durationsMap.put("64th", GPDuration.NOTE_64);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return GP7Rhythm.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	private GPDuration mapDuration(final String noteValue) {
		if (durationsMap.containsKey(noteValue)) {
			return durationsMap.get(noteValue);
		}

		Logger.error("Unknown GP7 noteValue " + noteValue + ", returning quarter note");
		return GPDuration.NOTE_4;
	}

	@Override
	public GP7Rhythm unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final GP7Rhythm gp7Rhythm = new GP7Rhythm();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			switch (reader.getNodeName()) {
				case "NoteValue":
					gp7Rhythm.duration = mapDuration(reader.getValue());
					break;
				case "AugmentationDot":
					gp7Rhythm.dots = Integer.valueOf(reader.getAttribute("count"));
					break;
				case "PrimaryTuplet":
					gp7Rhythm.primaryTuplet = new GP7Tuplet(Integer.valueOf(reader.getAttribute("num")),
							Integer.valueOf(reader.getAttribute("den")));
					break;
				case "SecondaryTuplet":
					gp7Rhythm.secondaryTuplet = new GP7Tuplet(Integer.valueOf(reader.getAttribute("num")),
							Integer.valueOf(reader.getAttribute("den")));
					break;
				default:
					Logger.error("Unknown GP7Rhythm property " + reader.getNodeName());
					break;

			}
			reader.moveUp();
		}

		return gp7Rhythm;
	}

}
