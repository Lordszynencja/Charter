package log.charter.io.gp.gp7.converters;

import java.util.ArrayList;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.gp.gp7.data.GP7Beat;

public class GP7BeatConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return GP7Beat.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	@Override
	public GP7Beat unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final GP7Beat beat = new GP7Beat();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			switch (reader.getNodeName()) {
				case "Tremolo":
					beat.tremolo = true;
					break;
				case "Rhythm":
					beat.rhythmReference = Integer.valueOf(reader.getAttribute("ref"));
					break;
				case "Notes":
					beat.notes = new ArrayList<>();
					for (final String noteId : reader.getValue().split(" ")) {
						try {
							beat.notes.add(Integer.valueOf(noteId));
						} catch (final NumberFormatException e) {
						}
					}
					break;
				default:
					break;
			}
			reader.moveUp();
		}

		return beat;
	}

}
