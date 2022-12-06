package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.XStream;

import log.charter.io.rs.xml.converters.ChordTemplateConverter;
import log.charter.io.rs.xml.converters.CountedListConverter;
import log.charter.io.rs.xml.converters.NullSafeIntegerConverter;

public class SongXStreamHandler {
	private static XStream xstream = prepareXStream();

	private static XStream prepareXStream() {
		final XStream xstream = new XStream();
		xstream.registerConverter(new ChordTemplateConverter());
		xstream.registerConverter(new CountedListConverter());
		xstream.registerConverter(new NullSafeIntegerConverter());
		xstream.ignoreUnknownElements();
		xstream.processAnnotations(Song.class);
		xstream.allowTypes(new Class[] { //
				Anchor.class, //
				BendValue.class, //
				Chord.class, //
				ChordNote.class, //
				ChordTemplate.class, //
				EBeat.class, //
				HandShape.class, //
				Level.class, //
				Note.class, //
				Phrase.class, //
				PhraseIteration.class, //
				Song.class });

		return xstream;
	}

	public static Song readSong(final String xml) {
		return (Song) xstream.fromXML(xml);
	}

	public static String saveSong(final Song song) {
		return xstream.toXML(song);
	}
}
