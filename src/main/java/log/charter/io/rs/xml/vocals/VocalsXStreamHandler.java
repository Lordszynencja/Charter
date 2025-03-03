package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.CountedListConverter;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.io.rs.xml.converters.VocalsConverter;
import log.charter.util.collections.ArrayList2;
import log.charter.util.collections.HashMap2;

public class VocalsXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new CountedListConverter());
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new VocalsConverter());
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList2.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap2.class));
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(ArrangementVocals.class);
		xstream.allowTypes(new Class[] { //
				ArrangementVocals.class, //
				ArrangementVocal.class });

		return xstream;
	}

	public static ArrangementVocals readVocals(final String xml) {
		if (xml == null || xml.isEmpty()) {
			return new ArrangementVocals();
		}

		return (ArrangementVocals) xstream.fromXML(xml);
	}

	public static String saveVocals(final ArrangementVocals vocals) {
		return XMLHandler.generateXML(xstream, vocals);
	}
}
