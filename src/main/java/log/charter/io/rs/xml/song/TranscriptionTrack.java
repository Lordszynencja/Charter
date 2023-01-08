package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;

public class TranscriptionTrack {
	@XStreamAsAttribute
	public int difficulty = -1;

	public CountedList<ArrangementNote> notes = new CountedList<>();
	public CountedList<ArrangementChord> chords = new CountedList<>();
	public CountedList<ArrangementAnchor> anchors = new CountedList<>();
	public CountedList<ArrangementHandShape> handShapes = new CountedList<>();
}
