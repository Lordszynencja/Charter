package log.charter.io.gp.gp7;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.io.XMLHandler;
import log.charter.io.gp.gp7.data.GP7Automation;
import log.charter.io.gp.gp7.data.GP7Bar;
import log.charter.io.gp.gp7.data.GP7Beat;
import log.charter.io.gp.gp7.data.GP7MasterBar;
import log.charter.io.gp.gp7.data.GP7MasterTrack;
import log.charter.io.gp.gp7.data.GP7Note;
import log.charter.io.gp.gp7.data.GP7Rhythm;
import log.charter.io.gp.gp7.data.GP7Score;
import log.charter.io.gp.gp7.data.GP7Staff;
import log.charter.io.gp.gp7.data.GP7Track;
import log.charter.io.gp.gp7.data.GP7Tuplet;
import log.charter.io.gp.gp7.data.GP7Voice;
import log.charter.io.gp.gp7.data.GPIF;

public class GP7FileXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap.class));

		xstream.ignoreUnknownElements();
		xstream.processAnnotations(GPIF.class);
		xstream.allowTypes(new Class[] { //
				GP7Automation.class, //
				GP7Bar.class, //
				GP7Beat.class, //
				GP7MasterBar.class, //
				GP7MasterTrack.class, //
				GP7Note.class, //
				GP7Rhythm.class, //
				GP7Score.class, //
				GP7Staff.class, //
				GP7Track.class, //
				GP7Tuplet.class, //
				GP7Voice.class, //
				GPIF.class });

		return xstream;
	}

	public static GPIF readGPIF(final File file) {
		return (GPIF) xstream.fromXML(GP7ZipReader.readTabXML(file));
	}

	public static String saveGPIF(final GPIF songArrangement) {
		return XMLHandler.generateXML(xstream, songArrangement);
	}
}
