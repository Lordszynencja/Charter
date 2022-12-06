package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.XStream;

import log.charter.io.rs.xml.converters.ChordTemplateConverter;
import log.charter.io.rs.xml.converters.CountedListConverter;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.io.rs.xml.converters.VocalsConverter;

public class VocalsXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new ChordTemplateConverter());
		xstream.registerConverter(new CountedListConverter());
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new VocalsConverter());
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(Vocals.class);
		xstream.allowTypes(new Class[] { //
				Vocals.class, //
				Vocal.class });

		return xstream;
	}

	public static Vocals readVocals(final String xml) {
		return (Vocals) xstream.fromXML(xml);
	}

	public static String saveVocals(final Vocals vocals) {
		return xstream.toXML(vocals);
	}
}
