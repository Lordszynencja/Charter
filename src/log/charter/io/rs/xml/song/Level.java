package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.io.rs.xml.converters.CountedListConverter.CountedList;

@XStreamAlias("level")
public class Level {
	@XStreamAsAttribute
	public int difficulty;

	public CountedList<Anchor> anchors;
	public CountedList<HandShape> handShapes;
	public CountedList<Chord> chords;
	public CountedList<Note> notes;
}
