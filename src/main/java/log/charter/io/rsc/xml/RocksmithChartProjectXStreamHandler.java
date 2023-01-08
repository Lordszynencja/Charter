package log.charter.io.rsc.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.data.managers.modes.EditMode;
import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.song.Beat;
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
				Beat.class });

		return xstream;
	}

	public static RocksmithChartProject readProject(final String xml) {
		final RocksmithChartProject project = (RocksmithChartProject) xstream.fromXML(xml);

		if (project.chartFormatVersion == 1) {
			project.editMode = EditMode.GUITAR;
			project.arrangement = 0;
			project.level = 0;
			project.time = 0;

			project.chartFormatVersion = 2;
		}

		return project;
	}

	public static String saveProject(final RocksmithChartProject rocksmithChartProject) {
		return XMLHandler.generateXML(xstream, rocksmithChartProject);
	}
}
