package log.charter.io.rs.xml.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;

import log.charter.data.song.position.time.IPosition;
import log.charter.io.rs.xml.converters.TimeConverter;

@XStreamAlias("handShape")
public class ArrangementHandShape implements IPosition {
	@XStreamAsAttribute
	public int chordId;
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int startTime;
	@XStreamAsAttribute
	@XStreamConverter(TimeConverter.class)
	public int endTime;

	public ArrangementHandShape(final int chordId, final int startTime, final int endTime) {
		this.chordId = chordId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public int position() {
		return startTime;
	}

	@Override
	public void position(final int newPosition) {
		startTime = newPosition;
	}
}
