package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.song.ArrangementHandShape;
import log.charter.song.notes.Chord;
import log.charter.song.notes.PositionWithLength;

@XStreamAlias("handShape")
public class HandShape extends PositionWithLength {
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
