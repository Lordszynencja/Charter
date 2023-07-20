package log.charter.data.copySystem.data;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.data.copySystem.data.positions.CopiedAnchorPosition;
import log.charter.data.copySystem.data.positions.CopiedArrangementEventsPointPosition;
import log.charter.data.copySystem.data.positions.CopiedHandShapePosition;
import log.charter.data.copySystem.data.positions.CopiedSoundPosition;
import log.charter.data.copySystem.data.positions.CopiedToneChangePosition;
import log.charter.data.copySystem.data.positions.CopiedVocalPosition;
import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.song.ChordTemplate;
import log.charter.song.Phrase;
import log.charter.song.ToneChange;
import log.charter.song.notes.GuitarSound;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class CopyDataXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList2.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap2.class));
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(CopyData.class);
		xstream.processAnnotations(CopyData.class);
		xstream.allowTypes(new Class[] { //
				AnchorsCopyData.class, //
				EventPointsCopyData.class, //
				ChordTemplate.class, //
				CopyData.class, //
				CopiedAnchorPosition.class, //
				CopiedArrangementEventsPointPosition.class, //
				CopiedHandShapePosition.class, //
				CopiedSoundPosition.class, //
				CopiedToneChangePosition.class, //
				CopiedVocalPosition.class, //
				FullGuitarCopyData.class, //
				GuitarSound.class, //
				HandShapesCopyData.class, //
				Phrase.class, //
				SoundsCopyData.class, //
				ToneChange.class, //
				VocalsCopyData.class });

		return xstream;
	}

	public static CopyData readProject(final String xml) {
		final Object o = xstream.fromXML(xml);
		if (o.getClass().isAssignableFrom(CopyData.class)) {
			return (CopyData) o;
		}

		return null;
	}

	public static String saveProject(final CopyData copyData) {
		return XMLHandler.generateXML(xstream, copyData);
	}
}
