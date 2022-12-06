package log.charter.io.rs.xml.song;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;

public class TranscriptionTrack {
	public CountedList<Note> notes;
	public CountedList<Chord> chords;
	public CountedList<HandShape> handShapes;
	public CountedList<Anchor> anchors;
}
