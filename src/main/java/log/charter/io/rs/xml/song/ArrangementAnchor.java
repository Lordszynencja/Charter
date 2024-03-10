package log.charter.io.rs.xml.song;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.Anchor;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("anchor")
public class ArrangementAnchor {
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int time;
	@XStreamAsAttribute
	public int fret;
	@XStreamAsAttribute
	public BigDecimal width;

	public ArrangementAnchor() {
	}

	public ArrangementAnchor(final Anchor anchor) {
		time = anchor.position();
		fret = anchor.fret;
		width = new BigDecimal(anchor.width);
	}
}
