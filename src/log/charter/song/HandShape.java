package log.charter.song;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import log.charter.io.rs.xml.song.ArrangementHandShape;
import log.charter.song.notes.Chord;
import log.charter.song.notes.PositionWithLength;

@XStreamAlias("handShape")
public class HandShape extends PositionWithLength {
	public int chordId;

	public HandShape(final int position, final int length) {
		super(position, length);
		chordId = -1;
	}

	public HandShape(final int chordId) {
		super(0, 0);
		this.chordId = chordId;
	}

	public HandShape(final ArrangementHandShape arrangementHandShape) {
		super(arrangementHandShape.startTime, arrangementHandShape.endTime - arrangementHandShape.startTime);
		chordId = arrangementHandShape.chordId;
	}

	public HandShape(final HandShape other) {
		super(other);
		chordId = other.chordId;
	}

	public HandShape(final Chord chord, final int length) {
		super(chord.position(), length);
		chordId = chord.chordId;
	}
}
