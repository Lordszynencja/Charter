package log.charter.io.rs.xml;

import com.thoughtworks.xstream.XStream;

import log.charter.io.rs.xml.vocals.Vocal;
import log.charter.io.rs.xml.vocals.Vocals;

public class XMLHandler {
	private static final XStream xstream = createXStream();

	private static XStream createXStream() {
		final XStream xstream = new XStream();
		xstream.processAnnotations(Vocals.class);
		xstream.allowTypes(new Class[] { //
				Vocals.class, //
				Vocal.class });

		return xstream;
	}

	private static String generateXML(final Object object) {
		return "<?xml version='1.0' encoding='UTF-8'?>\n" //
				+ xstream.toXML(object);
	}

	public static String generateXML(final Vocals vocals) {
		return generateXML((Object) vocals);
	}
}
