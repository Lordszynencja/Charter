package log.charter.io.rs.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.rs.xml.vocals.Vocal;
import log.charter.io.rs.xml.vocals.Vocals;
import log.charter.util.CollectionUtils.ArrayList2;

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
		final ArrayList2<Vocal> list = (ArrayList2<Vocal>) context.convertAnother(null, ArrayList2.class);
		return new Vocals(list);
	}

}