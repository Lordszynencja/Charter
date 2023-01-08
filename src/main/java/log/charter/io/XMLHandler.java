package log.charter.io;

import com.thoughtworks.xstream.XStream;

public class XMLHandler {
	public static String generateXML(final XStream xstream, final Object object) {
		return "<?xml version='1.0' encoding='UTF-8'?>\n" //
				+ xstream.toXML(object);
	}
}
