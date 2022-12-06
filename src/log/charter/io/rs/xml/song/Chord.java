package log.charter.io.rs.xml.song;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("chord")
@XStreamInclude(ChordNote.class)
public class Chord {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public int chordId;
	@XStreamAsAttribute
	public Integer palmMute;
	@XStreamAsAttribute
	public Integer fretHandMute;
	@XStreamAsAttribute
	public Integer accent;
	@XStreamImplicit
	public List<ChordNote> chordNotes;
}
