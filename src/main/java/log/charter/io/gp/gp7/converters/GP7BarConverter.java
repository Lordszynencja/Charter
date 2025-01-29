package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.gp.gp7.data.GP7Bar;

public class GP7BarConverter implements Converter {
	private final GP7IntegerListConverter gp7IntegerListConverter = new GP7IntegerListConverter();

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return GP7Bar.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final GP7Bar gp7Bar = new GP7Bar();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			switch (reader.getNodeName()) {
				case "Voices":
					gp7Bar.voices = gp7IntegerListConverter.fromString(reader.getValue());
					break;
				default:
					break;

			}
			reader.moveUp();
		}

		return gp7Bar;
	}

}
