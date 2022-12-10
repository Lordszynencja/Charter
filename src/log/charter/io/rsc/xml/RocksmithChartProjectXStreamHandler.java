package log.charter.io.rsc.xml;

import java.awt.Event;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.song.Beat;
import log.charter.song.Phrase;
import log.charter.song.PhraseIteration;
import log.charter.song.Section;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class RocksmithChartProjectXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList2.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap2.class));
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(RocksmithChartProject.class);
		xstream.allowTypes(new Class[] { //
				RocksmithChartProject.class, //
				Beat.class, //
				Event.class, //
				Phrase.class, //
				PhraseIteration.class, //
				Section.class });

		return xstream;
	}

	public static RocksmithChartProject readProject(final String xml) {
		return (RocksmithChartProject) xstream.fromXML(xml);
	}

	public static String saveProject(final RocksmithChartProject rocksmithChartProject) {
		return XMLHandler.generateXML(xstream, rocksmithChartProject);
	}
}
