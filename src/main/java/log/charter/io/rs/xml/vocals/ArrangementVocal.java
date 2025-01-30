package log.charter.io.rs.xml.vocals;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.io.rs.xml.converters.TimeConverter;

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
	@XStreamAsAttribute
	public int tone;

	public ArrangementVocal(final Integer time, final Integer length, final String lyric, final int tone) {
		this.time = time;
		this.length = length;
		this.lyric = lyric;
		this.tone = tone;
	}
}
