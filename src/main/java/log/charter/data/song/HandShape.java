package log.charter.data.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import log.charter.data.song.notes.Chord;
import log.charter.data.song.position.PositionWithLength;
import log.charter.io.rs.xml.song.ArrangementHandShape;

@XStreamAlias("handShape")
public class HandShape extends PositionWithLength {
	@XStreamAsAttribute
	public int templateId;

	public HandShape(final int position, final int length) {
		super(position, length);
		templateId = -1;
	}

	public HandShape(final int templateId) {
		super(0, 0);
		this.templateId = templateId;
	}

	public HandShape(final ArrangementHandShape arrangementHandShape) {
		super(arrangementHandShape.startTime, arrangementHandShape.endTime - arrangementHandShape.startTime);
		templateId = arrangementHandShape.chordId;
	}

	public HandShape(final HandShape other) {
		super(other);
		templateId = other.templateId;
	}

	public HandShape(final Chord chord, final int length) {
		super(chord.position(), length);
		templateId = chord.templateId();
	}
}
