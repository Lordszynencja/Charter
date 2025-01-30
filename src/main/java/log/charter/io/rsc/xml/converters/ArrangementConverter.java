package log.charter.io.rsc.xml.converters;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

import log.charter.data.song.Arrangement;

public class ArrangementConverter extends ReflectionConverter {
	public ArrangementConverter(final Mapper mapper, final ReflectionProvider reflectionProvider) {
		super(mapper, reflectionProvider);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return Arrangement.class.equals(type);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		super.marshal(source, writer, context);
	}

	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final String baseTone = reader.getAttribute("baseTone");

		final Arrangement arrangement = (Arrangement) super.unmarshal(reader, context);
		if (baseTone != null) {
			arrangement.startingTone = baseTone;
		}

		return arrangement;
	}

}
