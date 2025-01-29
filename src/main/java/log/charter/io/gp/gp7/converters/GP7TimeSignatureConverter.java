package log.charter.io.gp.gp7.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.util.data.TimeSignature;

public class GP7TimeSignatureConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return TimeSignature.class.isAssignableFrom(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
	}

	@Override
	public TimeSignature unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final String[] tokens = reader.getValue().split("/");

		return new TimeSignature(Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1]));
	}

}
