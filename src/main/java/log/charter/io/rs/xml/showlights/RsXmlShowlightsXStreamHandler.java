package log.charter.io.rs.xml.showlights;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.CountedListConverter;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.io.rs.xml.converters.RsXmlShowlightsConverter;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashMap2;

public class RsXmlShowlightsXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new CountedListConverter());
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new RsXmlShowlightsConverter());
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList2.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap2.class));
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(RsXmlShowlights.class);
		xstream.allowTypes(new Class[] { //
				RsXmlShowlights.class, //
				RsXmlShowlight.class });

		return xstream;
	}

	public static RsXmlShowlights readShowlights(final String xml) {
		if (xml == null || xml.isEmpty()) {
			return new RsXmlShowlights();
		}

		return (RsXmlShowlights) xstream.fromXML(xml);
	}

	public static String saveShowlights(final RsXmlShowlights showlights) {
		return XMLHandler.generateXML(xstream, showlights);
	}
}
