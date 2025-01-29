package log.charter.io.gp.gp7.converters;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class GP7PropertiesConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return true;
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	private Map<String, String> readValues(final HierarchicalStreamReader reader) {
		final Map<String, String> values = new HashMap<>();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			values.put(reader.getNodeName(), reader.getValue());
			reader.moveUp();
		}
		return values;
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final Map<String, Map<String, String>> result = new HashMap<>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if (reader.getNodeName().equals("Property")) {
				result.put(reader.getAttribute("name"), readValues(reader));
			}
			reader.moveUp();
		}

		return result;
	}

}
