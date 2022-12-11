package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;
import log.charter.song.Vocal;

@XStreamAlias("vocal")
public class ArrangementVocal {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer time;
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public Integer length;
	@XStreamAsAttribute
	public String lyric;

	public ArrangementVocal() {
	}

	public ArrangementVocal(final Vocal vocal) {
		time = vocal.position;
		length = vocal.length;
		lyric = vocal.lyric;
	}
}
