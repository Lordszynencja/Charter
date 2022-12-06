package log.charter.io.rs.xml.converters;

import java.util.List;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.rs.xml.vocals.Vocal;
import log.charter.io.rs.xml.vocals.Vocals;

public class VocalsConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(Vocals.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final Vocals vocals = (Vocals) source;
		writer.addAttribute("count", "" + vocals.vocals.size());
		context.convertAnother(vocals.vocals);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final List<Vocal> list = (List<Vocal>) context.convertAnother(null, List.class);
		return new Vocals(list);
	}

}