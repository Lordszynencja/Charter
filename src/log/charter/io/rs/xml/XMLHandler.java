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
}
