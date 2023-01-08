package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;

import log.charter.io.XMLHandler;
import log.charter.io.rs.xml.converters.ChordTemplateConverter;
import log.charter.io.rs.xml.converters.CountedListConverter;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;
import log.charter.util.CollectionUtils.ArrayList2;
import log.charter.util.CollectionUtils.HashMap2;

public class SongArrangementXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new ChordTemplateConverter());
		xstream.registerConverter(new CountedListConverter());
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.registerConverter(new CollectionConverter(xstream.getMapper(), ArrayList2.class));
		xstream.registerConverter(new MapConverter(xstream.getMapper(), HashMap2.class));

		xstream.ignoreUnknownElements();
		xstream.processAnnotations(SongArrangement.class);
		xstream.allowTypes(new Class[] { //
				ArrangementAnchor.class, //
				ArrangementBendValue.class, //
				ArrangementChord.class, //
				ArrangementChordNote.class, //
				ArrangementChordTemplate.class, //
				EBeat.class, //
				ArrangementEvent.class, //
				ArrangementHandShape.class, //
				ArrangementLevel.class, //
				ArrangementNote.class, //
				ArrangementPhrase.class, //
				ArrangementPhraseIteration.class, //
				ArrangementSection.class, //
				ArrangementTone.class, //
				SongArrangement.class });

		return xstream;
	}

	public static SongArrangement readSong(final String xml) {
		return (SongArrangement) xstream.fromXML(xml);
	}

	public static String saveSong(final SongArrangement songArrangement) {
		return XMLHandler.generateXML(xstream, songArrangement);
	}
}
