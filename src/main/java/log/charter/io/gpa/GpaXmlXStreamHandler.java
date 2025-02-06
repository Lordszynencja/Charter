package log.charter.io.gpa;

import java.io.File;

import com.thoughtworks.xstream.XStream;

import log.charter.util.RW;

public class GpaXmlXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(GpaTrack.class);
		xstream.allowTypes(new Class[] { //
				GpaSyncPoint.class, //
				GpaTrack.class });

		return xstream;
	}

	public static GpaTrack readGpaTrack(final File file) {
		final String content = RW.read(file);

		final Object o = xstream.fromXML(content);
		if (!o.getClass().isAssignableFrom(GpaTrack.class)) {
			return null;
		}

		return (GpaTrack) o;
	}
}
