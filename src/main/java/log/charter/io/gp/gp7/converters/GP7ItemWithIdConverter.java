package log.charter.io.gp.gp7.converters;

import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public abstract class GP7ItemWithIdConverter implements Converter {
	protected abstract Converter getItemConverter();

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return true;
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final Map<Integer, Object> result = new HashMap<>();

		while (reader.hasMoreChildren()) {
			reader.moveDown();
			final Integer id = Integer.valueOf(reader.getAttribute("id"));
			final Object item = getItemConverter().unmarshal(reader, context);
			result.put(id, item);
			reader.moveUp();
		}

		return result;
	}

}
