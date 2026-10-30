package log.charter.io.workTimes.xml;

import java.io.File;
import java.util.Map;

import com.thoughtworks.xstream.XStream;

import log.charter.data.WorkTimes.ArrangementWorkTime;
import log.charter.data.WorkTimes.ChartWorkTimes;
import log.charter.io.XMLHandler;
import log.charter.util.RW;

public class ChartWorkTimesXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(ChartWorkTimes.class);
		xstream.allowTypes(new Class[] { //
				ArrangementWorkTime.class, //
				ChartWorkTimes.class });

		return xstream;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, ChartWorkTimes> read(final File file) {
		final String content = RW.read(file, "UTF-8");

		final Object o = xstream.fromXML(content);
		if (!Map.class.isAssignableFrom(o.getClass())) {
			return null;
		}

		return (Map<String, ChartWorkTimes>) o;
	}

	public static String write(final Map<String, ChartWorkTimes> o) {
		return XMLHandler.generateXML(xstream, o);
	}

}
