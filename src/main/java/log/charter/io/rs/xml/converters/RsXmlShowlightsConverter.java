package log.charter.io.rs.xml.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import log.charter.io.rs.xml.showlights.RsXmlShowlight;
import log.charter.io.rs.xml.showlights.RsXmlShowlights;
import log.charter.util.collections.ArrayList2;

public class RsXmlShowlightsConverter implements Converter {
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(final Class type) {
		return type.equals(RsXmlShowlights.class);
	}

	@Override
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		final RsXmlShowlights showlights = (RsXmlShowlights) source;
		writer.addAttribute("count", "" + showlights.showlights.size());
		context.convertAnother(showlights.showlights);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		final ArrayList2<RsXmlShowlight> list = (ArrayList2<RsXmlShowlight>) context.convertAnother(null,
				ArrayList2.class);
		return new RsXmlShowlights(list);
	}

}