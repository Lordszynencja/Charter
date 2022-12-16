package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.song.ArrangementHandShape;

@XStreamAlias("handShape")
public class HandShape extends Position {
	public int length;

	public HandShape(final ArrangementHandShape arrangementHandShape) {
		super(arrangementHandShape.startTime);
		length = arrangementHandShape.endTime - position;
	}

	public HandShape(final HandShape other) {
		super(other);
		length = other.length;
	}
}
