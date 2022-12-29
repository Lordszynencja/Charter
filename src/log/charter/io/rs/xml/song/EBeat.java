package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.Beat;

@XStreamAlias("ebeat")
public class EBeat {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public Integer measure;

	public EBeat() {
	}

	public EBeat(final Beat beat) {
		time = beat.position();
		measure = beat.firstInMeasure ? 1 : null;
	}
}
