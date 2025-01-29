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

	public ArrangementHandShape(final int startTime, final int endTime, final int chordId) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.chordId = chordId;
	}

	@Override
	public double position() {
		return startTime;
	}

	@Override
	public void position(final double newPosition) {
		startTime = (int) newPosition;
	}
}
